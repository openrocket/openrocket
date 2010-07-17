%load('pink.mat');
%figure;
loglog(f, psdavg, 'k', f, S, 'k--')
xlabel('Frequency / Hz');
axis([0.03 10 5e-5 1])
set(gca,'XTickLabel', ['0.1'; ' 1 '; '10 '])
print('pinkfreq2.eps')
