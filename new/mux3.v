`timescale 1ns / 1ps
module mux3#(parameter WITHD = 32)(
    input       [WITHD-1:0]     d0,
    input       [WITHD-1:0]     d1, 
    input       [WITHD-1:0]     d2,
    input       [1:0]           s,
    output      [WITHD-1:0]     y   
    );
    assign y = s[1]?d2:(s[0]?d1:d0);
    
endmodule
