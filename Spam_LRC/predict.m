function p = predict(theta, X)

m = size(X, 1);
p = zeros(m, 1);

A = sigmoid(X * theta);

one = find(A >= 0.5);
zero = find(A < 0.5);
A(one, 1) = 1;
A(zero, 1) = 0;

p = A;

end
