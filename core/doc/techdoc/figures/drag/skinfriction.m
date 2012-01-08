Rlam = 10.^(4:0.05:7);
Rturb = 10.^(4:0.05:8);

Rcrit = 539154;
Rtrans = 10.^(5:0.05:8);
Rtrans = [Rcrit Rtrans(find(Rtrans > Rcrit))];

Rs=3e-6;
Lr=1.0;
Rcrit=[51*(Rs/Lr)^(-1.039) 1e8];
critical = 0.032*(Rs/Lr)^(0.2) * [1 1];
Rcrit

laminar = 1.328./sqrt(Rlam);
turbulent = 1./(1.50*log(Rturb)-5.6).^2;
transitional = 1./((1.50*log(Rtrans)-5.6).^2) - 1700./Rtrans;

figure;

loglog(Rlam,laminar,'k-.',Rturb,turbulent,'k--',Rtrans,transitional,'k:',Rcrit,critical,'k-');
axis([1e4 1e8 0.0005 0.02]);
xlabel('Reynolds number');
ylabel('Skin friction coefficient');
legend('Laminar','Turbulent','Transitional','Roughness-limited');

set(gcf, 'PaperPositionMode', 'manual');
set(gcf, 'PaperUnits', 'centimeters');
set(gcf, 'PaperPosition', [2 2 15 7.5]);

print('skinfriction.eps');
