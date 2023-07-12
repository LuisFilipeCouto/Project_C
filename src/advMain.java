import java.io.IOException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.File;
import java.io.PrintWriter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.stringtemplate.v4.*;

public class advMain {
   public static void main(String[] args) {

      if (args.length == 0) {
         System.err.println("Error: No filename provided");
         System.exit(1);
      }
      
      String inFile = args[0];
      String outputName = "../examples/bin/" + args[0].substring(args[0].lastIndexOf('/') + 1).split("\\.")[0] + ".py";

      try {
         // create a CharStream that reads from standard input:
         CharStream input = CharStreams.fromFileName(inFile);

         // create a lexer that feeds off of input CharStream:
         advLexer lexer = new advLexer(input);

         // create a buffer of tokens pulled from the lexer:
         CommonTokenStream tokens = new CommonTokenStream(lexer);

         // create a parser that feeds off the tokens buffer:
         advParser parser = new advParser(tokens);

         // begin parsing at program rule:
         ParseTree tree = parser.program();
         if (parser.getNumberOfSyntaxErrors() == 0) {
            advCompiler compiler = new advCompiler();
            ST result = compiler.visit(tree);
            result.add("name", "Output");

            // write to output python file
            PrintWriter pw = new PrintWriter(new File(outputName));
            pw.print(result.render());
            pw.close();
         }
      }
      catch(IOException e) {
         e.printStackTrace();
         System.exit(1);
      }
      catch(RecognitionException e) {
         e.printStackTrace();
         System.exit(1);
      }
   }
}