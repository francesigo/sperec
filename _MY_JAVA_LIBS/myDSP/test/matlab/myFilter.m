function [Y] = myFilter(b, a, X, zi)
if nargin<4
    [y, zf] = filter(b, a, X);
else
    [y, zf] = filter(b, a, X, zi);
end
Y.y = y;
Y.zf = zf;
