import matplotlib.pyplot as plt
import numpy as np
import math

m = 1000

def func_lin(x):
    y = -math.log(x)

    if x > m/2:
        return -math.log(m - x)
    return y



x = np.arange(0.01, m, .2 )
lin = []

for i in x:
    lin.append(func_lin(i))

plt.title('Podtrénovanie', fontsize=16)
plt.plot(x, lin)

plt.show()