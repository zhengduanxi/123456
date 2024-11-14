module get_cpu_pc (
  input wire [31:0] PCNext
);

import "DPI-C" function void get_cpu_pc(input int PCNext);

  always @(*) begin
        get_cpu_pc(PCNext);
    end
  
endmodule
