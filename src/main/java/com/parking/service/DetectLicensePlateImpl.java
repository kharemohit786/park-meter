package com.parking.service;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.parking.entity.ParkingVehicleDetails;
import com.parking.entity.SlotAvailability;
import com.parking.model.LicensePlateInfo;
import com.parking.model.NotificationBean;
import com.parking.repository.ParkingVehicleDetailsRepository;
import com.parking.repository.SlotAvailabilityRepository;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DetectLicensePlateImpl implements DetectLicensePlate {
    static AmazonRekognition rekognitionClient;
    private static String REGEX = "^[a-zA-Z]{2}[0-9]{2}[a-zA-Z]{2}[0-9]{4}$";
    private static String BUCKET_NAME = "hackathon-2020";
    
    private final ParkingVehicleDetailsRepository parkingVehicleDetailsRepository;
    private final SlotAvailabilityRepository slotAvailabilityRepository;
    
    @PostConstruct
    public void initialize()    {
        rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
    }

    public static boolean detectLabelsByImage(String s3Url) throws Exception {
        DetectLabelsRequest request = new DetectLabelsRequest().withImage(new Image().
                                                                          withS3Object(new S3Object()
                                                                                       .withName(FilenameUtils.getName(s3Url))
                                                                                       .withBucket(BUCKET_NAME))).withMaxLabels(10).withMinConfidence(77F);
        try {
            //detectTextFromImage(s3Url);
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List<Label> labels = result.getLabels();

            for (Label label : labels) {
                if(label.getName().equalsIgnoreCase("vehicle") ||
                                label.getName().equalsIgnoreCase("transportation")) {
                    return true;
                }
                System.out.println(label.getName() + ": " + label.getConfidence().toString());
            }

        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        }
        return false;

    }

   /* private static ByteBuffer getByteStream(String photo) throws IOException, FileNotFoundException {
        ByteBuffer imageBytes;
        ClassLoader classLoader = new DetectLicensePlateImpl().getClass().getClassLoader();
        try (InputStream inputStream = new FileInputStream(new File(classLoader.getResource(photo).getFile()))) {
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
        }
        return imageBytes;
    }*/

    private static LicensePlateInfo detectTextFromImage(String s3Url) throws Exception {
        DetectTextRequest request = new DetectTextRequest().withImage(new Image().withS3Object(new S3Object()
                                                                                               .withName(FilenameUtils.getName(s3Url))
                                                                                               .withBucket(BUCKET_NAME)));
        LicensePlateInfo licensePlateInfo = new LicensePlateInfo();
        try {
            //Boolean isVehicle = detectLabelsByImage(s3Url);
            Boolean isVehicle = true;
            if(!isVehicle)  {
                return licensePlateInfo;
            }
            DetectTextResult result = rekognitionClient.detectText(request);
            List<TextDetection> textDetections = result.getTextDetections();

            System.out.println("Detected lines and words for " + s3Url);
            for (TextDetection text : textDetections) {
                String removedSpace = text.getDetectedText().replaceAll(" ", "");
                if(removedSpace.matches(REGEX) ||
                               StringUtils.reverse(removedSpace).matches(REGEX) )  {
                    licensePlateInfo.setNumber(text.getDetectedText());
                    licensePlateInfo.setConfidenceScore(text.getConfidence());
                    licensePlateInfo.setType(text.getType());
                    break;
                }
               
            }
        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        }
        return licensePlateInfo;
    }


    @Override
    public void prcessFrames(NotificationBean notificationBean) {
        try {
            SlotAvailability slotAvailability;
            Boolean isParked = true;
            long elapsedTime = 0L;
            long updatedDiff = 0L;
            long currentTime = System.currentTimeMillis();
            Boolean isOldVehicle = false;
            LicensePlateInfo licensePlateInfo = detectTextFromImage(notificationBean.getS3Url());
            if(StringUtils.isEmpty(licensePlateInfo.getNumber()))    {
                return;
            }
            
            ParkingVehicleDetails parkingVehicleDetails = new ParkingVehicleDetails();
            Optional<ParkingVehicleDetails> ParkingVehicleDetailsOpt = parkingVehicleDetailsRepository.getByLicensePlate(licensePlateInfo.getNumber().replace(" ", ""));
            if(ParkingVehicleDetailsOpt.isPresent())    {
                isOldVehicle = true;
                parkingVehicleDetails = ParkingVehicleDetailsOpt.get();
                elapsedTime = parkingVehicleDetails.getCreatedDbTime();
                
                updatedDiff = currentTime - TimeUnit.MINUTES.toMillis(1);
                if(parkingVehicleDetails.getIsParked() && (updatedDiff > elapsedTime) && (updatedDiff >  parkingVehicleDetails.getLastModifiedDbTime())) {
                    parkingVehicleDetails.setLastModifiedDbTime(System.currentTimeMillis()/1000);
                    isParked = false;
                    log.info("Unparking...{}", licensePlateInfo.getNumber());
                } else if((updatedDiff > elapsedTime) && (updatedDiff >  parkingVehicleDetails.getLastModifiedDbTime()))  {
                    isParked = true;
                    log.info("Parking again...{}", licensePlateInfo.getNumber());
                }
            }
            parkingVehicleDetails.setIsParked(isParked);
            parkingVehicleDetails.setLicensePlate(licensePlateInfo.getNumber().replace(" ", ""));
            
            Optional<SlotAvailability> slotAvailabilityOpt= slotAvailabilityRepository.getByLocationName(notificationBean.getLocationId());
            if(!slotAvailabilityOpt.isPresent())   {
                SlotAvailability slots = new SlotAvailability();
                slots.setLocationName(notificationBean.getLocationId());
                slots.setUsedSlot(1);
                slots.setVehicleType("car");
                slotAvailability = slotAvailabilityRepository.save(slots);
            } else {
                slotAvailability = slotAvailabilityOpt.get();
                if(!isOldVehicle)    {
                    log.info("Car arrived: {}", parkingVehicleDetails.getLicensePlate());
                    slotAvailability.setUsedSlot(slotAvailability.getUsedSlot()+1);
                }
                if(!isParked && (updatedDiff > elapsedTime) && (updatedDiff >  parkingVehicleDetails.getLastModifiedDbTime()))    {
                    log.info("Car departed: {}", parkingVehicleDetails.getLicensePlate());
                    slotAvailability.setUsedSlot(slotAvailability.getUsedSlot()-1);
                }
                slotAvailabilityRepository.save(slotAvailability);
            }
            parkingVehicleDetails.setSlotAvailibilityId(slotAvailability.getId());
            if(!isOldVehicle)   {
                parkingVehicleDetails.setCreatedDbTime(System.currentTimeMillis()/1000);
            }
            
            parkingVehicleDetailsRepository.save(parkingVehicleDetails);
        } catch (Exception e) {
            log.info("Error parsing image {}", e.getMessage());
        }
    }

}
