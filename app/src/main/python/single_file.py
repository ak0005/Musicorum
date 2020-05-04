import numpy as np
import struct
import wave


def find_nearest(array, value):
    idx = (np.abs(array - value)).argmin()
    return array[idx]


############################## Initialize ##################################

def all_final(path):
    # Some Useful Variables
    window_size = 2205  # Size of window to be used for detecting silence
    beta = 1  # Silence detection parameter
    max_notes = 100  # Maximum number of notes in file, for efficiency
    sampling_freq = 44100  # Sampling frequency of audio signal
    threshold = 100
    # array = [1046.50, 1174.66, 1318.51, 1396.91, 1567.98, 1760.00, 1975.53,
    #          2093.00, 2349.32, 2637.02, 2793.83, 3135.96, 3520.00, 3951.07,
    #          4186.01, 4698.63, 5274.04, 5587.65, 6271.93, 7040.00, 7902.13]
    array = [4186.01, 3951.07, 3729.31, 3520.00, 3322.44, 3135.96, 2959.96, 2793.83, 2637.02,
             2489.02, 2349.32, 2217.46, 2093.00, 1975.53, 1864.66, 1760.00, 1661.22, 1567.98,
             1479.98, 1396.91, 1318.51, 1244.51, 1174.66, 1108.73, 1046.50, 987.767, 932.328,
             880.000, 830.609, 783.991, 739.989, 698.456, 659.255, 622.254, 587.330, 554.365,
             523.251, 493.883, 466.164, 440.000, 415.305, 391.995, 369.994, 349.228, 329.628,
             311.127, 293.665, 277.183, 261.626, 246.942, 233.082, 220.000, 207.652, 195.998,
             184.997, 174.614, 164.814, 155.563, 146.832, 138.591, 130.813, 123.471, 116.541,
             110.000, 103.826, 97.9989, 92.4986, 87.3071, 82.4069, 77.7817, 73.4162, 69.2957,
             65.4064, 61.7354, 58.2705, 55.0000, 51.9130, 48.9995, 46.2493, 43.6536, 41.2035,
             38.8909, 36.7081, 34.6479, 32.7032, 30.8677, 29.1353, 27.5000]

    notes = ["C8", "B7", "A#7", "A7", "G#7", "G7", "F#7", "F7", "E7", "D#7", "D7", "C#7", "C7",
             "B6", "A#6", "A6", "G#6", "G6", "F#6", "F6", "E6", "D#6", "D6", "C#6", "C6", "B5",
             "A#5", "A5", "G#5", "G5", "F#5", "F5", "E5", "D#5", "D5", "C#5", "C5", "B4", "A#4",
             "A4", "G#4", "G4", "F#4", "F4", "E4", "D#4", "D4", "C#4", "C4", "B3", "A#3", "A3",
             "G#3", "G3", "F#3", "F3", "E3", "D#3", "D3", "C#3", "C3", "B2", "A#2", "A2", "G#2",
             "G2", "F#2", "F2", "E2", "D#2", "D2", "C#2", "C2", "B1", "A#1", "A1", "G#1", "G1",
             "F#1", "F1", "E1", "D#1", "D1", "C#1", "C1", "B0", "A#0", "A0"]
    Identified_Notes = []

    ############################## Read Audio File #############################
    # print ('\n\nReading Audio File...')

    sound_file = wave.open(path, 'r')
    file_length = sound_file.getnframes()

    sound = np.zeros(file_length)
    mean_square = []
    sound_square = np.zeros(file_length)
    for i in range(file_length):
        data = sound_file.readframes(1)
        # print(data)
        # temp = list(struct.unpack("<h", data[:2]))
        # print(temp)
        # temp[0]+=struct.unpack("<h", data[2:])[0]
        # print(temp)
        # data=tuple(temp)
        # data=data[0]+data[1]
        # print(data.__class__)

        try:
            som = struct.unpack("<h", data)
            data = som
        except Exception:
            temp = list(struct.unpack("<h", data[:2]))
            temp[0] += struct.unpack("<h", data[2:])[0]
            # for i in range(2,len(data)//2+1,2):
            # 	#print(i)
            # 	temp[0]+=struct.unpack("<h", data[i:i+2])[0]
            # 	#temp[0]+=struct.unpack("<h", data[i+2:])[0]
            data = tuple(temp)
        # print(data)
        sound[i] = int(data[0])
    # exit(0)

    sound = np.divide(sound, float(2 ** 15))  # Normalize data in range -1 to 1

    ######################### DETECTING SCILENCE ##################################

    sound_square = np.square(sound)
    frequency = []
    dft = []
    i = 0
    j = 0
    k = 0
    # traversing sound_square array with a fixed window_size
    while (i <= len(sound_square) - window_size):
        s = 0.0
        j = 0
        while (j < window_size):
            s = s + sound_square[i + j]
            j = j + 1
        # detecting the silence waves
        if s < threshold:
            if (i - k > window_size * 4):
                dft = np.array(dft)  # applying fourier transform function
                dft = np.fft.fft(sound[k:i])
                dft = np.argsort(dft)
                if (dft[0] > dft[-1] and dft[1] > dft[-1]):
                    i_max = dft[-1]
                elif (dft[1] > dft[0] and dft[-1] > dft[0]):
                    i_max = dft[0]
                else:
                    i_max = dft[1]
                # claculating frequency
                frequency.append((i_max * sampling_freq) / (i - k))
                dft = []
                k = i + 1
        i = i + window_size

    print('length', len(frequency))
    print("frequency")

    for i in frequency:
        # print(i)
        idx = (np.abs(array - i)).argmin()
        if (idx != 87):
            Identified_Notes.append(notes[idx])
    return Identified_Notes
# print(Identified_Notes)
