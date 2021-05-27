import os
import wave

if __name__ == '__main__':
    directory = 'data/financial_speech_dataset_id/antar'
    (_, _, filenames) = next(os.walk(directory))
    bad_files = []
    for filename in filenames:
        wave_filename = os.path.join(directory, filename)
        try:
            with wave.open(wave_filename, 'r') as file:
                print("Parameters: ", file.getparams())
        except wave.Error as e:
            bad_file_tuple = (filename, e)
            bad_files.append(bad_file_tuple)

    print("Found {} bad files".format(len(bad_files)))
    print(bad_files)