%function res = my_svd (X, debug) % Useful for debug
function res = my_svd_testJava(X)

[U, D, V] = svd(X); % [U,S,V] = svd(A) performs a singular value decomposition of matrix A, such that A = U*S*V'.

% Quello che viene dopo serve a replicare in Java gli stessi valori
segni = sign(V(1, :));
V2 = bsxfun(@times, V, segni);
U2 = bsxfun(@times, U, segni);

% E' possibile verificare che U2 * D * V2 = X
matlab_check_my_svd_testJava = max(max((X-U2*D*V2')./X))

res.U = U2;
res.D = D;
res.V = V2;
