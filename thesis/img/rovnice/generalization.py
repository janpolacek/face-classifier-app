import matplotlib.pyplot as plt
import numpy as np
import math


def func_lin(x):
    return x+ 1


def func_kv(x):
    return x * x


def func_9(x):
    return -pow(x, 9) + 2*pow(x, 7)


x = np.arange(-2., 2., 0.1)

lin = func_lin(x)
kv = func_kv(x)
dim9 = func_9(x)

points = np.array([0, 1, 1.3247, -1.4656])
p_lin = func_lin(points)
p_kv = func_kv(points)
p_dim9 = func_9(points)


plt.title('Podtrénovanie', fontsize=16)
plt.plot(x, lin)
# plt.plot(x, kv)
# plt.plot(x, dim9)
plt.plot(points, p_kv, 'ro')
plt.axis((-1.8, 1.8, -0.1, 2.5))

frame1 = plt.gca()
frame1.axes.get_xaxis().set_visible(False)
frame1.axes.get_yaxis().set_visible(False)

plt.show()