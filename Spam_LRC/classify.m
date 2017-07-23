clear;

data = load('~/workspace/457Classifier/Spam_LRC/test.txt');

X_train = data([1 : end - 150], [1 : end - 1]);
y_train = data([1 : end - 150], end);
X_test = data([end - 150 : end], [1 : end - 1]);
y_test = data([end - 150 : end], end);

[m_train, n] = size(X_train);
[m_test, n] = size(X_test);

X_train = [ones(m_train, 1) X_train];
X_test = [ones(m_test, 1) X_test];
init_theta = zeros(n + 1, 1);

options = optimset('GradObj', 'on', 'MaxIter', 100);
[theta, cost] = fminunc(@(t)(costFunction(t, X_train, y_train)), init_theta, options);

p = predict(theta, X_test);
accuracy = mean(double(p == y_test));
