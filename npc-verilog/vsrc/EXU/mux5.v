`timescale 1ns / 1ps
module mux5#(parameter WITHD = 32)(
    input       [WITHD-1:0]     d0,
    input       [WITHD-1:0]     d1, 
    input       [WITHD-1:0]     d2,
    input       [WITHD-1:0]     d3,
    input       [WITHD-1:0]     d4,
    input       [2:0]           s,
    output      [WITHD-1:0]     y   
    );
    assign y = s[2]?d4:(s[1]?(s[0]?d3:d2):(s[0]?d1:d0));
    
endmodule
