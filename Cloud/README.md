# FAVI-prototype in Cloud

-Save the dataset into Cloud Storage   
-Train model in AI Platform  
-Store the model in to Cloud Storage  
-Deploy the model  
-Store the Prediction in to Firestore  

## Dataset 

https://console.cloud.google.com/storage/browser/financial_speech_dataset_id

## Deploy Models on AI Platform and Save the Model in to Cloud Storage
CLOUD_PROJECT = 'your-project-id-here'  
BUCKET_NAME = 'favi-models'  
!gcloud config set project $CLOUD_PROJECT  
!gsutil mb $BUCKET_NAME  
model.save(BUCKET_NAME, save_format='tf')  
MODEL = 'favi_speech_model'  
!gcloud ai-platform models create $MODEL --regions=asia-southeast1  
VERSION = 'v1'  
MODEL_DIR = BUCKET_NAME  
!gcloud ai-platform versions create $VERSION \
  --model $MODEL \
  --origin $MODEL_DIR \
  --runtime-version=2.1 \
  --framework='tensorflow' \
  --python-version=3.7

## Create Deployment from Cloud Storage Trigger

gcloud functions deploy hello_gcs \
--runtime python37 \
--trigger-resource user_voice_input \
--trigger-event google.storage.object.finalize

## Store the Prediction in to Firestore

add this file below in to main.py  


from google.cloud import firestore

fname=audio_file.replace('/tmp/','')  
    output_fname=str(fname.replace('.wav',''))  
    db = firestore.Client()  
    doc_ref = db.collection(u'users').document(output_fname)  
    doc_ref.update({  
    u'prediction': prediction,  
    u'lastPrediction': prediction,  
    })  

