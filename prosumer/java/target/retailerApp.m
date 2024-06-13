function  bid_new = retailerApp(bid_old,a0,a1,a2,gamma,p_out)
 disp('Start to run retailerApp.m-------------------');

%update retailer side bid %---------------------------------------------------------------------------
warning('off','all');
x=bid_old;
%bid_retailer_new2 = zeros(1,4);
    %pi_retailer = -a2* p_out^ 2 * x^2 + (p_out^2 - a1 * p_out) * x - a0;     %Profit fundtion of retailers;
    diff = -2 * a2 * p_out^2 * x + p_out^2 - a1 * p_out;
    bid_new = max(x + gamma * diff,0);
    U_retailer = -a2 * p_out^2 * bid_new^2 + (p_out^2 - a1 * p_out) * bid_new - a0;
    disp(U_retailer);
 disp('Finish runing retailerApp.m-------------------');

