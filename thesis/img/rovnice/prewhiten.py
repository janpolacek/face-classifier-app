import numpy as np
from scipy import misc
import matplotlib.pyplot as plt

def prewhiten(x):
    mean = np.mean(x)
    std = np.std(x)
    std_adj = np.maximum(std, 1.0/np.sqrt(x.size))
    y = np.multiply(np.subtract(x, mean), 1/std_adj)
    return y


f, axarr = plt.subplots(ncols=2)
axarr[0].imshow(original)
axarr[1].imshow(norm, cmap='gray', interpolation='none')

plt.show()