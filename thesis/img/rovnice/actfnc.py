import matplotlib.pyplot as plt
import numpy as np
import math


def sigmoid(x):
    a = []
    for item in x:
        a.append(1/(1+math.exp(-item)))
    return a

x = np.arange(-5., 5., 0.1)
zero = np.zeros(len(x))


sig = sigmoid(x)
tanh = np.tanh(x)
relu = np.max([zero, x], axis=0)


# plt.title('Sigmoid')
# plt.plot(x, sig)

# plt.title('Tanh')
# plt.plot(x, tanh)

plt.title('ReLU')
plt.plot(x, relu)

plt.show()