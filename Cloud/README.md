# FAVI-prototype in Cloud

-Save the dataset into Cloud Storage
-Train model in AI Platform
-Save the model in to Cloud Storage
-Deploy the model
-Save the Prediction in to Firestore

## Create Deployment from Cloud Storage Trigger

gcloud functions deploy hello_gcs \
--runtime python37 \
--trigger-resource user_voice_input \
--trigger-event google.storage.object.finalize
