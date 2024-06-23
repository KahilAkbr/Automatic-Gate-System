# Automatic Gate System
Android Application for Automatic gate system simulation using license plate detection

## Tech Used
- Firebase Realtime Database : Used for listen to ultrasonic sensor and adding registered license plate
- Blynk API : Used for communicate between android app and IoT devices
- License Plate Recognizer API : Used for recognizing license plate number from image sent (https://guides.platerecognizer.com/docs/snapshot/api-reference/)

## How to Use
1. Connect your app with your own firebase by replacing google-service.json with yours\
2. in local.properties\
BASE_URL = [BLYNK_API_BASE_URL]\
BASE_URL_PLATE_READER = [LICENSE_PLATE_RECOGNIZER_URL]\
TOKEN_BLYNK = [YOUR_BLYNK_TOKEN]\
TOKEN_READER = [YOUR_LICENSE_PLATE_RECOGNIZER_TOKEN]

## IoT Device
![WhatsApp Image 2024-06-08 at 12 09 53](https://github.com/KahilAkbr/Automatic-Gate-System/assets/108219818/3b20a82f-bd54-4a2b-9c5c-7833b61aef5b)


## APP Interface
![WhatsApp Image 2024-06-09 at 05 31 37](https://github.com/KahilAkbr/Automatic-Gate-System/assets/108219818/0f2bc304-4f4d-4b4c-bce0-70241e3795b1)
![WhatsApp Image 2024-06-09 at 21 24 30](https://github.com/KahilAkbr/Automatic-Gate-System/assets/108219818/7ebd2858-f69a-48b7-9409-75c027396a45)
