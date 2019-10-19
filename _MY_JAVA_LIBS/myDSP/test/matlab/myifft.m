function y = myifft(re, im)
x = re + i*im;
y = ifft(x);