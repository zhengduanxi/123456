module difftest_ok (
  input wire [31:0] valid
);

import "DPI-C" function void difftest_ok(input int valid);

  always @(*) begin
        difftest_ok(valid);
    end
  
endmodule
