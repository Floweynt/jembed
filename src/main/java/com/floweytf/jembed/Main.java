package com.floweytf.jembed;

import com.floweytf.jembed.lang.CompilerFrontend;
import com.floweytf.jembed.lang.source.Source;

public class Main {
    public static void main(String[] args) {
        var compiler = new CompilerFrontend();
        compiler.parse(
            new Source(
                "<source>",
                """
                    import "/dev/null";
                    import "/sys/io";
                    import "@io";
                    import "@uwu";
                    import "@colo3";
                                        
                    func foo(var a: string, var x: int = 1) {
                    }
                            
                    func main() {
                        var x = "a" + 1;
                        var y = x + "a";
                        foo(y);
                    }
                    """
            )
        );
    }
}