%load('pink.mat');
%figure;

s = 7400;
max = t(length(t));

ax = [0 max -5 5];
ytick = -4:2:4;

subplot(2,1,1);
hold off;
plot(t, pink2(s:(s+length(t)-1)), 'k');
hold on;
plot([0 max], [0 0], 'k:');
axis(ax)
set(gca,'XTick', 0:5:max)
set(gca,'XTickLabel', [])
set(gca,'YTick', ytick)
ylabel('2 poles');

%subplot(3,1,2);
%hold off;
%plot(t, pink3(s:(s+length(t)-1)), 'k');
%hold on;
%plot([0 max], [0 0], 'k:');
%axis(ax)
%set(gca,'XTick', 0:5:max)
%set(gca,'XTickLabel', [])
%set(gca,'YTick', ytick)
%ylabel('5 poles');

subplot(2,1,2);
hold off;
plot(t, pink10(s:(s+length(t)-1)), 'k');
hold on;
plot([0 max], [0 0], 'k:');
axis(ax)
set(gca,'XTick', 0:5:max)
set(gca,'YTick', ytick)
ylabel('10 poles');

xlabel('Time / s');
print('pinktimesub.eps')
