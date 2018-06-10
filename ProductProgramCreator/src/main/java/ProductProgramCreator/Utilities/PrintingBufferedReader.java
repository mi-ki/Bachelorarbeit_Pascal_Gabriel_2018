package ProductProgramCreator.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class PrintingBufferedReader extends BufferedReader {
    public PrintingBufferedReader(Reader in, int sz) {
        super(in, sz);
    }

    public PrintingBufferedReader(Reader in) {
        super(in);
    }
    @Override
    public String readLine() throws IOException {
        String input = super.readLine();
        System.out.println("> " + input);
        return input;
    }
}
