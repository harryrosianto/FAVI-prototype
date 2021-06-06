### Prerequisites

Function dependencies used in this project:

- Keras 2.4.3
- matplotlib 3.2.2
- numpy 1.19.5
- pandas 1.1.5
- tensorflow 2.5.0
- google-cloud-storage 1.16.1
- librosa 0.8.0
- google-cloud-firestore 2.1.1
- google-api-python-client 2.7.0

### Dataset use
Favi Speech Commands Dataset (id). This dataset consists of over 5,800 WAV audio files of people saying 29 different words in Bahasa. This data was collected by The Late Nite Studio Team.

Link to the dataset:

https://storage.googleapis.com/financial_speech_dataset_id/data/favi_speech_dataset_v01.zip
https://storage.googleapis.com/financial_speech_dataset_id/data/favi_speech_dataset_v02.zip
https://storage.googleapis.com/financial_speech_dataset_id/favi_dataset02.json

### Classes
At this time, only 12 classes will be picked for the Android speech commands application.
`satu dua tiga empat lima enam tujuh delapan sembilan nol transfer tambah`

### Audio Processing
Data generation involves producing raw PCM wavform data containing a desired number of samples and at fixed sample rate and the following configuration is used

| Samples        | Sample Rate           | Clip Duration (ms)  |
| ------------- |:-------------:| -----:|
| 16000      | 16000 | 1000 |

### Built With

* [Keras](https://keras.io/) - Deep Learning Framework
* [TensorFlow](http://tensorflow.org/) - Machine Learning Library
