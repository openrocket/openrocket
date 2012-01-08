load('pink.mat');
figure;
plot(t, pink2(4001:4401), 'y:', t, pink10(4001:4401), 'k', t, pink50(4001:4401), 'y--')
xlabel('Time / s');
legend('  2 poles','10 poles','50 poles')
axis([0 20 -2.5 2.5])
set(gca,'XTick', 0:5:20)
set(gca,'YTick', -2:2)
print('pinktime.eps')
