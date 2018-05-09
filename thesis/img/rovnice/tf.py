
import tensorflow as tf
BATCH_SIZE = 10
NUM_STEPS = 100

# 1.Vytvor graf reprezentujci model.
x = tf.placeholder(tf.float32, [BATCH_SIZE, 784])  # Miesto (placeholder) pre vstup.
y = tf.placeholder(tf.float32, [BATCH_SIZE, 10])  # Placeholder pre triedy.

W_1 = tf.Variable(tf.random_uniform([784, 100]))  # 784x100 matica vah.
b_1 = tf.Variable(tf.zeros([100]))  # 100-prvkovy bias vektor.
layer_1 = tf.nn.relu(tf.matmul(x, W_1) + b_1)  # Vystup skrytej vrstvy.

W_2 = tf.Variable(tf.random_uniform([100, 10]))  # 100x10 matica vah.
b_2 = tf.Variable(tf.zeros([10]))  # 10-element bias vector.
layer_2 = tf.matmul(layer_1, W_2) + b_2  # Vystup linearnej vrstvy.

# 2. Pridaj uzly reprezentujuce algoritmus minimalizacie chyby
loss = tf.nn.softmax_cross_entropy_with_logits(layer_2, y)
train_op = tf.train.AdagradOptimizer(0.01).minimize(loss)

# 3. Spusti graph na davkach vstupnych dat
with tf.Session() as sess:  # Vytvorenie session.
    sess.run(tf.initialize_all_variables())  # Inicializacia vah na nahodne hodnoty.
    for step in range(NUM_STEPS):  # Trenuj v cykle.
        x_data, y_data = ...  # Nacitaj davku vstupnych dat.
        sess.run(train_op, {x: x_data, y: y_data})  # Vykonaj jeden treningovy cyklus.