import matplotlib.pyplot as plt
import numpy as np
import math


def fnc(item):
    return item*item*.5


def y_fnc(x):
    a = []
    for item in x:
        a.append(fnc(item))
    return a

x = np.arange(-2., 2., 0.1)
y = x
y2 = y_fnc(x)


plt.title('Gradient descent')
plt.plot(x, y2, '--', label=r'$f(x) = \frac {1}{2} x^2$')
plt.plot(x, y, label=r"$f'(x) =x$")
plt.legend(loc='lower right')
plt.text(-2.0, -0.5, r"Pre $x < 0$ máme $f'(x) < 0$," "\n" "takže  f znížime posunom" "\n" "vpravo.")
plt.plot([-1.2, -1.2], [fnc(-1.2), 0])
plt.text(.5, -0.5, r"Pre $x > 0$ máme $f'(x) > 0$," "\n" "takže  f znížime posunom" "\n" "vľavo.")
plt.plot([1.2, 1.2], [fnc(1.2), 0])
plt.text(-.8, 1.25, r"Globálne minimum v $x = 0$." "\n" "Keďže $f'(x) = 0$," "\n" "gradient descent končí tu.")
plt.plot([0, 0], [1.25, 0])

plt.show()