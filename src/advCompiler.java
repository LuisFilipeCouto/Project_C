import java.util.*;
import org.stringtemplate.v4.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import types.Type;

public class advCompiler extends advBaseVisitor<ST> {

   // Import stg file that contains the python templates
   private STGroup templates = new STGroupFile("python.stg");

   // Current parent automaton
   private String parentAutomaton = null;

   // Current parent view
   private String parentView = null;

   // Current parent animation
   private String parentAnimation = null;

   // Current parent viewport
   private String parentViewport = null;

   // Indicate which context we are currently in
   private String parentContext = null;

   // Map adv variable names to their generated python code names
   private HashMap<String, String> variableMap = new HashMap<String, String>();

   // Map local variables to parent context variables
   private HashMap<String, HashMap<String, String>> childVariables = new HashMap<String, HashMap<String, String>>();
   private HashMap<String, HashMap<String, String>> states = new HashMap<String, HashMap<String, String>>();

   private int numVars = 0;

   private String newVar() {
      numVars++;
      return "v" + numVars;
   }

   private String convertToPythonString(String str) {
      return "\"" + str + "\"";
   }

   @Override
   public ST visitProgram(advParser.ProgramContext ctx) {
      ST res = templates.getInstanceOf("module");

      // Visit all children, except when there is only one (one children indicates an
      // EOF, meaning an empty file)
      if (ctx.getChildCount() > 1) {
         res.add("stat", visit(ctx.alphabetDefinition()).render());

         Iterator<advParser.StatementContext> list = ctx.statement().iterator();
         while (list.hasNext()) {
            res.add("stat", visit(list.next()).render());
         }
      }
      return res;
   }

   @Override
   public ST visitAlphabetDefinition(advParser.AlphabetDefinitionContext ctx) {
      String alphabetSet = "";
      for (TerminalNode t : ctx.SYMBOL()) {
         alphabetSet += t.getText() + ", ";
      }
      alphabetSet = "[" + alphabetSet.substring(0, alphabetSet.length() - 2) + "]";
      String var = newVar();

      ST res = templates.getInstanceOf("decl");
      res.add("var", var);
      res.add("value", alphabetSet);

      variableMap.put("alphabet", var);

      return res;
   }

   @Override
   public ST visitStatement(advParser.StatementContext ctx) {
      ST res = templates.getInstanceOf("stats");
      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      return res;
   }

   @Override
   public ST visitSection(advParser.SectionContext ctx) {
      ST res = templates.getInstanceOf("stats");
      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      return res;
   }

   @Override
   public ST visitInstruction(advParser.InstructionContext ctx) {
      ST res = templates.getInstanceOf("stats");
      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      return res;
   }

   @Override
   public ST visitExpressionPoint(advParser.ExpressionPointContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", visit(ctx.point()).render());
      return res;
   }

   @Override
   public ST visitExpressionPow(advParser.ExpressionPowContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", visit(ctx.expression(0)).render() + " ** " + visit(ctx.expression(1)).render());
      return res;
   }

   @Override
   public ST visitExpressionMultDivMod(advParser.ExpressionMultDivModContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value",
            visit(ctx.expression(0)).render() + " " + ctx.op.getText() + " " + visit(ctx.expression(1)).render());
      return res;
   }

   @Override
   public ST visitExpressionSymbol(advParser.ExpressionSymbolContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", ctx.SYMBOL().getText());
      return res;
   }

   @Override
   public ST visitExpressionParenthesis(advParser.ExpressionParenthesisContext ctx) {

      // If the expression between parenthesis is a state, get its point of origin
      if(states.get(parentView).containsKey(ctx.expression().getText())) {
         ST res = templates.getInstanceOf("init");
         res.add("value", states.get(parentView).get(ctx.expression().getText()) + ".reference_point");
         return res;
      }
      else {
         ST res = templates.getInstanceOf("init");
         res.add("value", "(" + visit(ctx.expression()).render() + ")");
         return res;
      }
   }

   @Override
   public ST visitExpressionReadInput(advParser.ExpressionReadInputContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", visit(ctx.readInput()).render());
      return res;
   }

   @Override
   public ST visitExpressionAddSub(advParser.ExpressionAddSubContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value",
            visit(ctx.expression(0)).render() + " " + ctx.op.getText() + " " + visit(ctx.expression(1)).render());
      return res;
   }

   @Override
   public ST visitExpressionConditional(advParser.ExpressionConditionalContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value",
            visit(ctx.expression(0)).render() + " " + ctx.op.getText() + " " + visit(ctx.expression(1)).render());
      return res;
   }

   @Override
   public ST visitExpressionNot(advParser.ExpressionNotContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", "not " + visit(ctx.expression()).render());
      return res;
   }

   @Override
   public ST visitExpressionSign(advParser.ExpressionSignContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", ctx.sign.getText() + " " + visit(ctx.expression()).render());
      return res;
   }

   @Override
   public ST visitExpressionBoolean(advParser.ExpressionBooleanContext ctx) {
      ST res = templates.getInstanceOf("init");

      if (ctx.BOOLEAN().getText().equals("true")) {
         res.add("value", "True");
      } else if (ctx.BOOLEAN().getText().equals("false")) {
         res.add("value", "False");
      } else {
         res.add("value", ctx.BOOLEAN().getText());
      }

      return res;
   }

   @Override
   public ST visitExpressionIdentifier(advParser.ExpressionIdentifierContext ctx) {
      ST res = templates.getInstanceOf("init");

      String var;
      if (variableMap.containsKey(ctx.IDENTIFIER().getText())) {
         var = variableMap.get(ctx.IDENTIFIER().getText());
      } else {
         var = childVariables.get(parentContext).get(ctx.IDENTIFIER().getText());
      }
      res.add("value", var);

      return res;
   }

   @Override
   public ST visitExpressionAnd(advParser.ExpressionAndContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", visit(ctx.expression(0)).render() + " and " + visit(ctx.expression(1)).render());
      return res;
   }

   @Override
   public ST visitExpressionOr(advParser.ExpressionOrContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", visit(ctx.expression(0)).render() + " or " + visit(ctx.expression(1)).render());
      return res;
   }

   @Override
   public ST visitExpressionNumber(advParser.ExpressionNumberContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", ctx.number().getText());
      return res;
   }

   @Override
   public ST visitExpressionString(advParser.ExpressionStringContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", ctx.STRING().getText());
      return res;
   }

   @Override
   public ST visitDeclare(advParser.DeclareContext ctx) {
      ST res = templates.getInstanceOf("stats");

      for (TerminalNode id : ctx.IDENTIFIER()) {
         ctx.var = newVar();

         // Save variable in variables map according to the context it was declared in
         if (parentContext == null) {
            variableMap.put(id.getText(), ctx.var);
         } else {
            childVariables.get(parentContext).put(id.getText(), ctx.var);
         }

         // State is special case of variable declaration
         if (ctx.type().getText().equals("state")) {
            ST res2 = templates.getInstanceOf("state");
            res2.add("var", ctx.var);
            res2.add("identifier", convertToPythonString(id.getText()));

            ST res3 = templates.getInstanceOf("callMethod");
            res3.add("obj", parentAutomaton);
            res3.add("method", "add_state");
            res3.add("args", ctx.var);

            res.add("stat", res2.render());
            res.add("stat", res3.render());
         } else {
            ST res2 = templates.getInstanceOf("decl");
            res2.add("var", ctx.var);
            res2.add("value", "None");

            res.add("stat", res2.render());
         }
      }
      return res;
   }

   @Override
   public ST visitAssign(advParser.AssignContext ctx) {
      ST res = templates.getInstanceOf("stats");

      int numVariables = ctx.IDENTIFIER().size();
      for (int i = 0; i < numVariables; i++) {

         if(variableMap.containsKey(ctx.IDENTIFIER(i).getText())) {
            ctx.var = variableMap.get(ctx.IDENTIFIER(i).getText());
         } 
         else if(childVariables.get(parentContext).containsKey(ctx.IDENTIFIER(i).getText())) {
            ctx.var = childVariables.get(parentContext).get(ctx.IDENTIFIER(i).getText());
         }
         else {
            ctx.var = newVar();
         }

         // Save variable in variables map according to the context it was declared in
         if (parentContext == null) {
            variableMap.put(ctx.IDENTIFIER(i).getText(), ctx.var);
         } else {
            childVariables.get(parentContext).put(ctx.IDENTIFIER(i).getText(), ctx.var);
         }

         ST res2 = templates.getInstanceOf("decl");
         res2.add("var", ctx.var);
         res2.add("value", visit(ctx.expression(i)).render());

         res.add("stat", res2.render());
      }
      return res;
   }

   @Override
   public ST visitAutomatonSection(advParser.AutomatonSectionContext ctx) {
      ST res = templates.getInstanceOf("stats");
      ST res2 = templates.getInstanceOf("automaton");
      
      ctx.var = newVar();
      variableMap.put(ctx.IDENTIFIER().getText(), ctx.var);
      childVariables.put(ctx.var, new HashMap<>());
      parentAutomaton = ctx.var;
      parentContext = ctx.var;

      res2.add("var", ctx.var);
      res2.add("identifier", convertToPythonString(ctx.IDENTIFIER().getText()));
      res2.add("alphabet", variableMap.get("alphabet"));

      String type = ctx.getChild(0).getText();

      if (type.equals("NFA")) {
         res2.add("type", convertToPythonString("nfa"));
      } else if (type.equals("DFA")) {
         res2.add("type", convertToPythonString("dfa"));
      } else {
         res2.add("type", convertToPythonString("complete_dfa"));
      }

      res.add("stat", res2.render());

      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      parentContext = null;
      return res;
   }

   @Override
   public ST visitViewSection(advParser.ViewSectionContext ctx) {
      ST res = templates.getInstanceOf("stats");

      ctx.var = newVar();
      variableMap.put(ctx.IDENTIFIER(0).getText(), ctx.var);
      childVariables.put(ctx.var, new HashMap<>());
      states.put(ctx.var, new HashMap<>());
      parentAutomaton = variableMap.get(ctx.IDENTIFIER(1).getText());
      parentView = ctx.var;
      parentContext = ctx.var;

      ST res2 = templates.getInstanceOf("view");
      res2.add("var", ctx.var);
      res2.add("identifier", convertToPythonString(ctx.IDENTIFIER(0).getText()));
      res2.add("associated_automaton", variableMap.get(ctx.IDENTIFIER(1).getText()));

      res.add("stat", res2.render());

      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }

      for (Map.Entry<String, String> entry : childVariables.get(parentAutomaton).entrySet()) {
         String key = entry.getKey();
         String value = entry.getValue();

         if (key.contains("<")) {
            String[] states = key.substring(1, key.length() - 1).split(",");
            String var = newVar();

            if (states[0].equals(states[1])) {
               ST res3 = templates.getInstanceOf("loopTransitionFigure");
               res3.add("var", var);
               res3.add("transition_obj", value);
               res3.add("origin", childVariables.get(parentView).get(states[0]) + ".reference_point");
               res.add("stat", res3.render());
            } else {
               ST res3 = templates.getInstanceOf("lineTransitionFigure");
               res3.add("var", var);
               res3.add("transition_obj", value);
               res3.add("origin", childVariables.get(parentView).get(states[0]) + ".reference_point");
               res3.add("destination", childVariables.get(parentView).get(states[1]) + ".reference_point");
               res.add("stat", res3.render());
            }

            ST res4 = templates.getInstanceOf("callMethod");
            res4.add("obj", parentView);
            res4.add("method", "addFigure");
            res4.add("args", convertToPythonString(key));
            res4.add("args", var);
            res.add("stat", res4.render());

            childVariables.get(parentView).put(key, var);
         }
      }

      parentContext = null;
      return res;
   }

   @Override
   public ST visitAnimationSection(advParser.AnimationSectionContext ctx) {
      ST res = templates.getInstanceOf("stats");

      ctx.var = newVar();
      variableMap.put(ctx.IDENTIFIER().getText(), ctx.var);
      childVariables.put(ctx.var, new HashMap<>());
      parentAnimation = ctx.var;
      parentContext = ctx.var;

      ST res2 = templates.getInstanceOf("animation");
      res2.add("var", ctx.var);
      res2.add("identifier", convertToPythonString(ctx.IDENTIFIER().getText()));

      res.add("stat", res2.render());

      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }

      parentContext = null;
      return res;
   }

   @Override
   public ST visitSegment(advParser.SegmentContext ctx) {
      ST res = templates.getInstanceOf("stats");
      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      return res;
   }

   @Override
   public ST visitAllowedAutomaton(advParser.AllowedAutomatonContext ctx) {
      ST res = templates.getInstanceOf("stats");
      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      return res;
   }

   @Override
   public ST visitAllowedView(advParser.AllowedViewContext ctx) {
      ST res = templates.getInstanceOf("stats");
      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      return res;
   }

   @Override
   public ST visitAllowedAnimation(advParser.AllowedAnimationContext ctx) {
      ST res = templates.getInstanceOf("stats");
      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      return res;
   }

   @Override
   public ST visitAllowedViewPort(advParser.AllowedViewPortContext ctx) {
      ST res = templates.getInstanceOf("stats");
      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      return res;
   }

   @Override
   public ST visitAllowedForEach(advParser.AllowedForEachContext ctx) {
      ST res = templates.getInstanceOf("stats");
      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      return res;
   }

   @Override
   public ST visitViewPortDefinition(advParser.ViewPortDefinitionContext ctx) {
      ST res2 = templates.getInstanceOf("viewport");
      String var = newVar();
      res2.add("var", var);
      res2.add("identifier", convertToPythonString(ctx.IDENTIFIER(0).getText()));
      res2.add("associated_view", variableMap.get(ctx.IDENTIFIER(1).getText()));
      res2.add("point1", visit(ctx.point(0)).render());
      res2.add("point2", visit(ctx.point(1)).render());

      childVariables.get(parentAnimation).put(ctx.IDENTIFIER(0).getText(), var);

      ST res = templates.getInstanceOf("callMethod");
      res.add("stat", res2.render());
      res.add("obj", parentAnimation);
      res.add("method", "add_viewport");
      res.add("args", var);

      return res;
   }

   @Override
   public ST visitViewPortAccess(advParser.ViewPortAccessContext ctx) {
      ST res = templates.getInstanceOf("stats");

      parentViewport = childVariables.get(parentAnimation).get(ctx.IDENTIFIER().getText());

      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      return res;
   }

   @Override
   public ST visitStateProperty(advParser.StatePropertyContext ctx) {
      ST res = templates.getInstanceOf("callMethod");

      String[] info = ctx.genericList().listElement(0).getText().toString().split("=");
      String[] args = { convertToPythonString(ctx.IDENTIFIER().getText()), convertToPythonString(info[0]),
            info[1].equals("true") ? "True" : "False" };

      res.add("obj", parentAutomaton);
      res.add("method", "modify_state");
      res.add("args", args);

      return res;
   }

   @Override
   public ST visitTransitionDefinition(advParser.TransitionDefinitionContext ctx) {
      ST res = templates.getInstanceOf("stats");

      Iterator<advParser.TransitionSequenceContext> list = ctx.transitionSequence().iterator();
      while (list.hasNext()) {
         res.add("stat", visit(list.next()).render());
      }

      return res;
   }

   @Override
   public ST visitTransitionSequence(advParser.TransitionSequenceContext ctx) {
      String var = newVar();
      String from_state = ctx.IDENTIFIER(0).getText();
      String to_state = ctx.IDENTIFIER(1).getText();
      String identifier = "<" + from_state + "," + to_state + ">";
      String symbols = "";

      if (ctx.SYMBOL().size() == 0) {
         symbols = "[]";
      } else {
         for (TerminalNode t : ctx.SYMBOL()) {
            symbols += t.getText() + ", ";
         }
         symbols = "[" + symbols.substring(0, symbols.length() - 2) + "]";
      }

      ST res2 = templates.getInstanceOf("transition");
      res2.add("var", var);
      res2.add("identifier", convertToPythonString(identifier));
      res2.add("from_state", convertToPythonString(from_state));
      res2.add("to_state", convertToPythonString(to_state));
      res2.add("symbols", symbols);

      ST res = templates.getInstanceOf("callMethod");
      res.add("stat", res2.render());
      res.add("obj", parentAutomaton);
      res.add("method", "add_transition");
      res.add("args", var);

      childVariables.get(parentAutomaton).put(identifier, var);

      return res;
   }

   @Override
   public ST visitPlaceState(advParser.PlaceStateContext ctx) {
      ST res = templates.getInstanceOf("stats");
      Iterator<advParser.StatePlacementContext> list = ctx.statePlacement().iterator();
      while (list.hasNext()) {
         res.add("stat", visit(list.next()).render());
      }
      return res;
   }

   @Override
   public ST visitPlaceTransition(advParser.PlaceTransitionContext ctx) {
      ST res = templates.getInstanceOf("stats");
      Iterator<advParser.TransitionPlacementContext> list = ctx.transitionPlacement().iterator();
      while (list.hasNext()) {
         res.add("stat", visit(list.next()).render());
      }
      return res;
   }

   @Override
   public ST visitStatePlacement(advParser.StatePlacementContext ctx) {
      ST res2 = templates.getInstanceOf("stateFigure");
      String var = newVar();
      res2.add("var", var);
      res2.add("state_obj", childVariables.get(parentAutomaton).get(ctx.IDENTIFIER(0).getText()));

      if (ctx.getChild(2) instanceof TerminalNode) {
         res2.add("origin", childVariables.get(parentView).get(ctx.getChild(2).getText()));
      } else {
         res2.add("origin", visit(ctx.getChild(2)).render());
      }

      ST res = templates.getInstanceOf("callMethod");
      res.add("stat", res2.render());
      res.add("obj", parentView);
      res.add("method", "addFigure");
      res.add("args", convertToPythonString(ctx.IDENTIFIER(0).getText()));
      res.add("args", var);

      childVariables.get(parentView).put(ctx.IDENTIFIER(0).getText(), var);
      states.get(parentView).put(ctx.IDENTIFIER(0).getText(), var);

      return res;
   }

   @Override
   public ST visitTransitionPlacement(advParser.TransitionPlacementContext ctx) {
      ST res = templates.getInstanceOf("stats");

      ST res2 = templates.getInstanceOf("modifyAtribute");
      res2.add("obj", childVariables.get(parentAutomaton)
            .get(ctx.labelConstruction().label().transitionConstruction().getText()));
      res2.add("attrib", "label_reference_point");

      if (ctx.getChild(2) instanceof TerminalNode) {
         res2.add("value", childVariables.get(parentView).get(ctx.getChild(2).getText()));
      } else {
         res2.add("value", visit(ctx.getChild(2)).render());
      }

      // If there are label alignment changes
      if (ctx.labelConstruction().genericList() != null) {
         res.add("stat", visit(ctx.labelConstruction()).render());
      }

      res.add("stat", res2.render());

      return res;
   }

   @Override
   public ST visitLabelConstruction(advParser.LabelConstructionContext ctx) {
      ST res = templates.getInstanceOf("modifyAtribute");
      res.add("obj", childVariables.get(parentAutomaton).get(ctx.label().transitionConstruction().getText()));
      res.add("attrib", "label_alignment");

      String position = ctx.genericList().listElement(0).getText().split("=")[1];

      switch (position.toLowerCase()) {
         case "above":
            res.add("value", "Align.ABOVE_CENTERED");
            break;

         case "abovecenter":
            res.add("value", "Align.ABOVE_CENTERED");
            break;

         case "below":
            res.add("value", "Align.BELOW_CENTERED");
            break;

         case "belowcenter":
            res.add("value", "Align.BELOW_CENTERED");
            break;

         case "left":
            res.add("value", "Align.ABOVE_LEFT");
            break;

         case "right":
            res.add("value", "Align.ABOVE_RIGHT");
            break;

         case "aboveleft":
            res.add("value", "Align.ABOVE_LEFT");
            break;

         case "aboveright":
            res.add("value", "Align.ABOVE_RIGHT");
            break;

         case "belowleft":
            res.add("value", "Align.BELOW_LEFT");
            break;

         case "belowright":
            res.add("value", "Align.BELOW_RIGHT");
            break;
      }

      return res;
   }

   @Override
   public ST visitShow(advParser.ShowContext ctx) {
      ST res = templates.getInstanceOf("stats");

      /*
       * IMPORTANT NOTE: we want to perform the propery change on the stateFigure
       * object and not on the state object itself
       * Because of this, we do not visit stateProperty (which modifies the state
       * object) and instead perform the operation here
       */
      if (ctx.stateProperty().size() > 0) {
         for (ParseTree child : ctx.stateProperty()) {
            String property = child.getChild(1).getText();
            String[] info = (property.substring(1, property.length() - 1)).split("=");

            ST res2 = templates.getInstanceOf("modifyAtribute");
            res2.add("obj", parentViewport + ".associated_view");
            res2.add("attrib", "figures[" + convertToPythonString(child.getChild(0).getText()) + "]." + info[0]);
            res2.add("value", info[1].equals("true") ? "True" : "False");

            ST res3 = templates.getInstanceOf("modifyAtribute");
            res3.add("obj", parentViewport + ".associated_view");
            res3.add("attrib", "figures[" + convertToPythonString(child.getChild(0).getText()) + "].visible");
            res3.add("value", "True");

            res.add("stat", res2.render());
            res.add("stat", res3.render());

         }
      }

      // If any state is specified, show it
      if (ctx.IDENTIFIER().size() > 0) {
         for (TerminalNode id : ctx.IDENTIFIER()) {
            ST res2 = templates.getInstanceOf("modifyAtribute");
            res2.add("obj", parentViewport + ".associated_view");
            res2.add("attrib", "figures[" + convertToPythonString(id.getText()) + "].visible");
            res2.add("value", "True");
            res.add("stat", res2.render());
         }
      }

       // If any transition is specified, show it
      if (ctx.transitionConstruction().size() > 0) {
         for (ParseTree child : ctx.transitionConstruction()) {
            ST res2 = templates.getInstanceOf("modifyAtribute");
            res2.add("obj", parentViewport + ".associated_view");
            res2.add("attrib", "figures[" + convertToPythonString(child.getText()) + "].visible");
            res2.add("value", "True");
            res.add("stat", res2.render());
         }
      }

      ST res2 = templates.getInstanceOf("callMethod");
      res2.add("obj", parentViewport + ".associated_view");
      res2.add("method", "draw");
      res2.add("args", parentViewport + ".port");

      ST res3 = templates.getInstanceOf("show");
      res3.add("animation", parentAnimation);
      res3.add("viewport", parentViewport);

      res.add("stat", res2.render());
      res.add("stat", res3.render());

      return res;
   }

   @Override
   public ST visitGridConstruction(advParser.GridConstructionContext ctx) {
      ST res3 = templates.getInstanceOf("grid");
      String var = newVar();
      res3.add("var", var);
      res3.add("identifier", convertToPythonString(ctx.IDENTIFIER().getText()));
      res3.add("dimensions", visit(ctx.point()).render());

      int numProperties = ctx.genericList().listElement().size();
      String[] args = new String[numProperties];

      for (int i = 0; i < numProperties; i++) {
         args[i] = ctx.genericList().listElement(i).getText().split("=")[1];
      }
      res3.add("step", args[0]);
      res3.add("margin", args[1]);
      res3.add("line", convertToPythonString(args[3]));

      switch (args[2].toLowerCase()) {
         case "red":
            res3.add("color", "Color.RED");
            break;

         case "green":
            res3.add("color", "Color.GREEN");
            break;

         case "blue":
            res3.add("color", "Color.BLUE");
            break;

         case "gray":
            res3.add("color", "Color.GRAY");
            break;
      }

      ST res2 = templates.getInstanceOf("gridFigure");
      String var2 = newVar();
      res2.add("var", var2);
      res2.add("grid_obj", var);

      childVariables.get(parentView).put(ctx.IDENTIFIER().getText(), var2);

      ST res = templates.getInstanceOf("callMethod");
      res.add("stat", res3.render());
      res.add("stat", res2.render());
      res.add("obj", parentView);
      res.add("method", "addFigure");
      res.add("args", convertToPythonString(ctx.IDENTIFIER().getText()));
      res.add("args", var2);

      return res;
   }

   @Override
   public ST visitPause(advParser.PauseContext ctx) {
      ST res = templates.getInstanceOf("pause");
      return res;
   }

   @Override
   public ST visitPlay(advParser.PlayContext ctx) {
      ST res = templates.getInstanceOf("play");
      res.add("animation", parentAnimation);
      return res;
   }

   @Override
   public ST visitReadInput(advParser.ReadInputContext ctx) {
      ST res = templates.getInstanceOf("readInput");
      if (ctx.STRING() != null) {
         res.add("prompt", ctx.STRING().getText());
      }
      return res;
   }

   /*
   @Override public ST visitForEachListSegment(advParser.ForEachListSegmentContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      //return res;
   }
   
   @Override public ST visitForEachList(advParser.ForEachListContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      //return res;
   }
    
   @Override public ST visitForEachSetSegment(advParser.ForEachSetSegmentContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      //return res;
   }

   @Override public ST visitForEachSet(advParser.ForEachSetContext ctx) {
      ST res = null;
      return visitChildren(ctx);
      //return res;
   }
   */

   @Override public ST visitConditional(advParser.ConditionalContext ctx) {
      ST res = templates.getInstanceOf("conditional");
      res.add("condition", visit(ctx.expression()).render());
      
      if (ctx.condLoopStats().size() > 0) {
         Iterator<advParser.CondLoopStatsContext> list = ctx.condLoopStats().iterator();
         while (list.hasNext()) {
            res.add("stat", visit(list.next()).render());
         }
      }
      else {
         res.add("stat", "pass");
      }

      return res;
   }

   @Override public ST visitCondLoopStats(advParser.CondLoopStatsContext ctx) {
      ST res = templates.getInstanceOf("stats");
      if (ctx.getChildCount() > 0) {
         for (ParseTree child : ctx.children) {
            ST childResult = visit(child);
            if (childResult != null) {
               res.add("stat", childResult.render());
            }
         }
      }
      return res;
   }

   @Override public ST visitWhileLoop(advParser.WhileLoopContext ctx) {
      ST res = templates.getInstanceOf("whileLoop");
      res.add("condition", visit(ctx.expression()).render());
      
      if (ctx.condLoopStats().size() > 0) {
         Iterator<advParser.CondLoopStatsContext> list = ctx.condLoopStats().iterator();
         while (list.hasNext()) {
            res.add("stat", visit(list.next()).render());
         }
      }
      else {
         res.add("stat", "pass");
      }

      return res;
   }

   @Override public ST visitBreakLoop(advParser.BreakLoopContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", "break");
      return res;
   }

   @Override public ST visitContinueLoop(advParser.ContinueLoopContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", "continue");
      return res;
   }

   @Override
   public ST visitNumber(advParser.NumberContext ctx) {
      ST res = templates.getInstanceOf("init");
      res.add("value", ctx.getText());
      return res;
   }

   @Override
   public ST visitPoint(advParser.PointContext ctx) {
      ST res = templates.getInstanceOf("point");
      res.add("x", ctx.x.getText());
      res.add("y", ctx.y.getText());

      if (ctx.sep.getText().equals(",")) {
         res.add("type", "1");
      } else {
         res.add("type", "0");
      }

      return res;
   }

   @Override
   public ST visitPrint(advParser.PrintContext ctx) {
      ST res = templates.getInstanceOf("printOutput");
      res.add("output", visit(ctx.expression()).render());
      return res;
   }

}
