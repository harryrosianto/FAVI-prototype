#firestore
from google.cloud import firestore

#deploy
from google.cloud import storage
from google.api_core.client_options import ClientOptions
import googleapiclient.discovery
import numpy as np
import librosa
import tensorflow as tf
import json

def hello_gcs(event, context):
    file = event

    def predict_json(project, region, model, instances, version=None):
        prefix = "{}-ml".format(region) if region else "ml"
        api_endpoint = "https://{}.googleapis.com".format(prefix)
        client_options = ClientOptions(api_endpoint=api_endpoint)
        service = googleapiclient.discovery.build('ml', 
                                                  'v1',
                                                  client_options=client_options
                                                  )
        name = 'projects/{}/models/{}'.format(project, model)

        if version is not None:
            name += '/versions/{}'.format(version)

        response = service.projects().predict(
            name=name,
            body={'instances': instances}
        ).execute()

        if 'error' in response:
            raise RuntimeError(response['error'])

        return response['predictions']

    _mapping = ["sembilan", "tiga", "tujuh", "satu", "delapan", "enam",
    "tambah", "transfer", "lima", "empat", "nol", "dua"
    ]
    SAMPLES_TO_CONSIDER = 16000

    def preprocess(file_path, num_mfcc=13, n_fft=2048, hop_length=512):

        #load audio file
        signal, sample_rate = librosa.load(file_path)

        if len(signal) >= SAMPLES_TO_CONSIDER:
            #ensure consistency of the length of the signal
            signal = signal[:SAMPLES_TO_CONSIDER]
  
            #extract MFCCs
            MFCCs = librosa.feature.mfcc(signal, sample_rate, n_mfcc=num_mfcc, n_fft=n_fft,
                                   hop_length=hop_length)
        return MFCCs.T


    def download(filename):
        download_client = storage.Client()
        bucket = download_client.get_bucket('user_voice_input')
        blob = bucket.blob(filename)
        audio_file = '/tmp/'+filename
        blob.download_to_filename(audio_file)
        return audio_file

    audio_file = download(file['name'])

    #preprocessing audio
    MFCCs = preprocess(audio_file)
    MFCCs = MFCCs[np.newaxis, ..., np.newaxis]

    # Predict sound data
    project = 'the-late-night-studio'
    region = 'asia-southeast1'
    model = 'favi_speech_model'
    version = 'v02'
    instances = MFCCs.tolist()
    test_predictions = predict_json(project, region, model, instances, version)
    idx = np.argmax(test_predictions[0])
    print(idx)

    prediction = _mapping[idx]
    print('favi prediction: {}'.format(prediction))
    response_json = {"Prediction": prediction}
 
#firestore

    fname=audio_file.replace('/tmp/','')
    output_fname=str(fname.replace('.wav',''))
    db = firestore.Client()
    doc_ref = db.collection(u'users').document(output_fname)
    doc_ref.update({
    u'prediction': prediction,
    u'lastPrediction': prediction,
    })