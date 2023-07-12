import java.util.*;

import javax.management.openmbean.OpenMBeanAttributeInfoSupport;

import types.*;
import org.antlr.v4.runtime.tree.TerminalNode;

@SuppressWarnings("CheckReturnValue")
public class advSemantic extends advBaseVisitor<Boolean> {

   private final BooleanType booleanType = new BooleanType();
   private final IntegerType integerType = new IntegerType();
   private final FloatType floatType = new FloatType();
   private final StringType stringType = new StringType();
   private final SymbolType symbolType = new SymbolType();
   private final ListType listType = new ListType();
   private final SetType setType = new SetType();
   private final NumberType numberType = new NumberType();
   private final PointType pointType = new PointType();
   private final StateType stateType = new StateType();
   private final AutomatonType automatonType = new AutomatonType();
   private final ViewType viewType = new ViewType();
   private final AnimationType animationType = new AnimationType();
   private final GridType gridType = new GridType();

   protected Set<String> alphabetSymbols = new HashSet<>();

   protected SymbolTable globalSymbolTable = new SymbolTable();

   protected SymbolTable currentSymbolTable = globalSymbolTable;

   protected String currentAutomaton = null;

   protected String currentAutomatonType = "NFA";

   @Override public Boolean visitProgram(advParser.ProgramContext ctx) {
      return visitChildren(ctx);
   }

   @Override public Boolean visitAlphabetDefinition(advParser.AlphabetDefinitionContext ctx) {
      alphabetSymbols.clear();
      Boolean res = true;
      for (TerminalNode symbolCtx : ctx.SYMBOL()) {
         res = visit(symbolCtx);
         String symbol = symbolCtx.getText();
         if(alphabetSymbols.contains(symbol)){
            ErrorHandling.printError(ctx, "repeating symbol " + symbol + " in alphabet definition!");
            res = false;
         } else {
            alphabetSymbols.add(symbol);
        }
      }
      return res;
   }

   @Override public Boolean visitStatement(advParser.StatementContext ctx) {
      return visitChildren(ctx);
   }

   @Override public Boolean visitSection(advParser.SectionContext ctx) {
      return visitChildren(ctx);
   }

   @Override public Boolean visitInstruction(advParser.InstructionContext ctx) {
      return visitChildren(ctx);
   }

   @Override public Boolean visitExpressionPoint(advParser.ExpressionPointContext ctx) {
      return visit(ctx.point());
   }

   @Override public Boolean visitExpressionPow(advParser.ExpressionPowContext ctx) {
      Boolean res = visit(ctx.e1) && visit(ctx.e2);
      if(res){
         Type resultType = getResultType(ctx.e1.t, ctx.e2.t, "^");
         if (resultType != null) {
            ctx.t = resultType;
         }else {
            ErrorHandling.printError(ctx, "Invalid operation or incompatible types for power operation. The power operator '^' requires numeric types. Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e1.t.name() + ">");
            res = false;
         }
      }
      return res;
   }

   @Override public Boolean visitExpressionMultDivMod(advParser.ExpressionMultDivModContext ctx) {
      Boolean res = visit(ctx.e1) && visit(ctx.e2);
      if(res){
         Type resultType = getResultType(ctx.e1.t, ctx.e2.t, ctx.op.getText());
         if (resultType != null) {
            ctx.t = resultType;
         }else {
             switch (ctx.op.getText()) {
               
               case "*":
                  ErrorHandling.printError(ctx, "Invalid operation or incompatible types for multiplication. Multiplication (*) requires numeric types. Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               case "/":
                  ErrorHandling.printError(ctx, "Invalid operation or incompatible types for division. Division (/) requires numeric types. Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               case "%":
                  ErrorHandling.printError(ctx, "Invalid operation or incompatible types for modulo. Modulo (%) requires integer types. Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               default:
                  break;
            }
            res = false;
         }
      }
      return res;
   }

   @Override public Boolean visitExpressionSymbol(advParser.ExpressionSymbolContext ctx) {
      ctx.t = symbolType;
      return true;
   }

   @Override public Boolean visitExpressionParenthesis(advParser.ExpressionParenthesisContext ctx) {
      Boolean res = visit(ctx.e);
      if (res)
         ctx.t = ctx.e.t;
      return res;
   }

   @Override public Boolean visitExpressionReadInput(advParser.ExpressionReadInputContext ctx) {
      return visitChildren(ctx);
   }

   @Override public Boolean visitExpressionAddSub(advParser.ExpressionAddSubContext ctx) {
      Boolean res = visit(ctx.e1) && visit(ctx.e2);
      if(res){
         Type resultType = getResultType(ctx.e1.t, ctx.e2.t, ctx.op.getText());
         if (resultType != null) {
            ctx.t = resultType;
         }else {
            switch (ctx.op.getText()) {
               case "+":
                  ErrorHandling.printError(ctx, "Invalid operation or incompatible types for addition. Addition (+) requires compatible numeric or point types. Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               case "-":
                  ErrorHandling.printError(ctx, "Invalid operation or incompatible types for subtraction. Subtraction (-) requires compatible numeric or point types. Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               default:
                  break;
            }
            res = false;
         }
      }
      return res;
   }

   @Override public Boolean visitExpressionConditional(advParser.ExpressionConditionalContext ctx) {
      Boolean res = visit(ctx.e1) && visit(ctx.e2);
      if(res){
         Type resultType = getResultType(ctx.e1.t, ctx.e2.t, ctx.op.getText());
         if (resultType != null) {
            ctx.t = resultType;
         }else {
            switch (ctx.op.getText()) {
               case "==":
                  ErrorHandling.printError(ctx, "Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               case "!=":
                  ErrorHandling.printError(ctx, "Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               case "<":
                  ErrorHandling.printError(ctx, "Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               case ">":
                  ErrorHandling.printError(ctx, "Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               case ">=":
                  ErrorHandling.printError(ctx, "Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               case "<=":
                  ErrorHandling.printError(ctx, "Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;   
               default:
                  break;
            }
            res = false;
         }
      }
      return res;
   }

   @Override public Boolean visitExpressionNot(advParser.ExpressionNotContext ctx) {
      Boolean res = visit(ctx.e);
      if(res){
         Boolean aux = isValidPrefixExpression(ctx.e.t, ctx.op.getText());
         if (aux) {
            ctx.t = ctx.e.t;
         }else {
            switch (ctx.op.getText()) {
               case "!":
                  ErrorHandling.printError(ctx, "Invalid operation or incompatible types for not operation. Not (!) requires a boolean type. Found type: <" + ctx.e.t.name() + ">");
                  break;
               default:
                  break;
            }
            res = false;
         }
      }
      return res;
   }

   @Override public Boolean visitExpressionBoolean(advParser.ExpressionBooleanContext ctx) {
      ctx.t = booleanType;
      return true;
   }

   @Override public Boolean visitExpressionIdentifier(advParser.ExpressionIdentifierContext ctx) {
      String key = ctx.IDENTIFIER().getText();

      if (!currentSymbolTable.contains(key)) {
         ErrorHandling.printError(ctx, "Undeclared variable: <" + key + ">");
         return false;
      }else{
         ctx.t = strToType(currentSymbolTable.getType(key));
      }
      return true;
   }

   @Override public Boolean visitExpressionAnd(advParser.ExpressionAndContext ctx) {
      Boolean res = visit(ctx.e1) && visit(ctx.e2);
      if(res){
         Type resultType = getResultType(ctx.e1.t, ctx.e2.t, ctx.op.getText());
         if (resultType != null) {
            ctx.t = resultType;
         }else {
            switch (ctx.op.getText()) {
               case "&&":
                  ErrorHandling.printError(ctx, "Invalid operation or incompatible types for logical AND (&&) operation. Logical AND operation requires boolean operands. Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               default:
                  break;
            }
            res = false;
         }
      }
      return res;
   }

   @Override public Boolean visitExpressionOr(advParser.ExpressionOrContext ctx) {
      Boolean res = visit(ctx.e1) && visit(ctx.e2);
      if(res){
         Type resultType = getResultType(ctx.e1.t, ctx.e2.t, ctx.op.getText());
         if (resultType != null) {
            ctx.t = resultType;
         }else {
            switch (ctx.op.getText()) {
               case "&&":
                  ErrorHandling.printError(ctx, "Invalid operation or incompatible types for logical OR (||) operation. Logical OR operation requires boolean operands. Found types: <" + ctx.e1.t.name() + ">, <" + ctx.e2.t.name() + ">");
                  break;
               default:
                  break;
            }
            res = false;
         }
      }
      return res;
   }

   @Override public Boolean visitExpressionNumber(advParser.ExpressionNumberContext ctx) {
      return visit(ctx.number());
   }

   @Override public Boolean visitExpressionSign(advParser.ExpressionSignContext ctx) {
      Boolean res = visit(ctx.expression());
      if(res){
         Boolean aux = isValidPrefixExpression(ctx.expression().t, ctx.sign.getText());
         if (aux != null) {
            ctx.t = ctx.expression().t;
         }else {
            switch (ctx.sign.getText()) {
               case "+":
                  ErrorHandling.printError(ctx, "Invalid operation or incompatible types for unary plus (+) operation. Unary plus operation requires a numeric or a point type. Found type: <" + ctx.expression().t.name() + ">");
                  break;
               case "-":
                  ErrorHandling.printError(ctx, "Invalid operation or incompatible types for unary minus (-) operation. Unary minus operation requires a numeric or a point type. Found type: <" + ctx.expression().t.name() + ">");
                  break;
               default:
                  break;
            }
            res = false;
         }
      }
      return res;
   }

   @Override public Boolean visitExpressionString(advParser.ExpressionStringContext ctx) {
      ctx.t = stringType;
      return true;
   }

   @Override public Boolean visitTypeBoolean(advParser.TypeBooleanContext ctx) {
      ctx.t = booleanType;
      return true;
   }

   @Override public Boolean visitTypeInteger(advParser.TypeIntegerContext ctx) {
      ctx.t = integerType;
      return true;
   }

   @Override public Boolean visitTypeFloat(advParser.TypeFloatContext ctx) {
      ctx.t = floatType;
      return true;
   }

   @Override public Boolean visitTypeString(advParser.TypeStringContext ctx) {
      ctx.t = stringType;
      return true;
   }

   @Override public Boolean visitTypeNumber(advParser.TypeNumberContext ctx) {
      ctx.t = numberType;
      return true;
   }

   @Override public Boolean visitTypePoint(advParser.TypePointContext ctx) {
      ctx.t = pointType;
      return true;
   }

   @Override public Boolean visitTypeState(advParser.TypeStateContext ctx) {
      ctx.t = stateType;
      return true;
   }

   @Override public Boolean visitDeclare(advParser.DeclareContext ctx) {
      Boolean res = visit(ctx.type());
      if(res){
         List<TerminalNode> identifiers = ctx.IDENTIFIER();

         for(TerminalNode idNode : identifiers){
            String key = idNode.getText();
            if(currentSymbolTable.contains(key)){
               ErrorHandling.printError(ctx, "The variable '" + key + "' is already defined.");
               res = false;
            } else {
               currentSymbolTable.addSymbol(key, ctx.type().t);
            }
         }
      }
      return res;
   }

   @Override public Boolean visitAssign(advParser.AssignContext ctx) {
      Boolean res = true;
      List<TerminalNode> ids = ctx.IDENTIFIER();
      List<advParser.ExpressionContext> exprs = ctx.expression();

      if(ctx.type() == null){

         for(int i = 0; i < ids.size(); i++){
            String key = ids.get(i).getText();
            if(!currentSymbolTable.contains(key)){
               ErrorHandling.printError(ctx, "Variable '" + key + "' has not been defined.");
               return false;
            }else{
               if(strToType(currentSymbolTable.getType(key)) != exprs.get(i).t){
                  ErrorHandling.printError(ctx, "Type mismatch: Cannot assign expression of type '" + exprs.get(i).t.name() + "' to variable '" + key + "' of type '" + ctx.type().t.name() + "'.");
                  return false;
               }
               res = visit(exprs.get(i));
               if(!res){
                  return res;
               }
            }
         }

      }else{

         for(int i = 0; i < ids.size(); i++){
            String key = ids.get(i).getText();
            if(currentSymbolTable.contains(key)){
               ErrorHandling.printError(ctx, "Variable '" + key + "' is already defined.");
               return false;
            }else{
               if(strToType(currentSymbolTable.getType(key)) != exprs.get(i).t){
                  ErrorHandling.printError(ctx, "Type mismatch: Cannot assign expression of type '" + exprs.get(i).t.name() + "' to variable '" + key + "' of type '" + ctx.type().t.name() + "'.");
                  return false;
               }else {
                  currentSymbolTable.addSymbol(key, ctx.type().t);
               }
               res = visit(exprs.get(i));
               if(!res){
                  return res;
               }
            }
         }
      }
      return res;
   }

   @Override public Boolean visitAutomatonSection(advParser.AutomatonSectionContext ctx) {
      Boolean res = true;
      if(globalSymbolTable.contains(ctx.IDENTIFIER().getText())){
         ErrorHandling.printError(ctx, "The variable '" + ctx.IDENTIFIER().getText() +  "' is already defined!");
         res = false;
      }else{
         globalSymbolTable.addSymbol(ctx.IDENTIFIER().getText(), automatonType);
         currentSymbolTable = new SymbolTable(currentSymbolTable);
         globalSymbolTable.addChild(ctx.IDENTIFIER().getText(), currentSymbolTable);
      }
      currentSymbolTable = globalSymbolTable;
      currentAutomatonType = ctx.getChild(0).getText();  //NFA DAF or complete DFA
      res = visit(ctx.segment());
      return res;
   }

   @Override public Boolean visitViewSection(advParser.ViewSectionContext ctx) {
      Boolean res = true;
      if(globalSymbolTable.contains(ctx.IDENTIFIER(0).getText())){
         ErrorHandling.printError(ctx, "The variable '" + ctx.IDENTIFIER(0).getText() +  "' is already defined!");
         res = false;
      }else if(!globalSymbolTable.contains(ctx.IDENTIFIER(1).getText())){
         ErrorHandling.printError(ctx, "The variable '" + ctx.IDENTIFIER(1).getText() +  "' is not defined!");
         res = false;
      }else{
         globalSymbolTable.addSymbol(ctx.IDENTIFIER(0).getText(), viewType);
         currentSymbolTable = new SymbolTable(currentSymbolTable);
         globalSymbolTable.addChild(ctx.IDENTIFIER(0).getText(), currentSymbolTable);
      }
      currentSymbolTable = globalSymbolTable;
      currentAutomaton = ctx.IDENTIFIER(1).getText();
      res = visit(ctx.segment());
      return res;
   }

   @Override public Boolean visitAnimationSection(advParser.AnimationSectionContext ctx) {
      Boolean res = true;
      if(globalSymbolTable.contains(ctx.IDENTIFIER().getText())){
         ErrorHandling.printError(ctx, "The variable '" + ctx.IDENTIFIER().getText() +  "' is already defined!");
         return false;
      }else{
         globalSymbolTable.addSymbol(ctx.IDENTIFIER().getText(), animationType);
         currentSymbolTable = new SymbolTable(currentSymbolTable);
         globalSymbolTable.addChild(ctx.IDENTIFIER().getText(), currentSymbolTable);
   }
      currentSymbolTable = globalSymbolTable;
      res = visit(ctx.segment());
      return res;
   }

   //-------------------

   @Override public Boolean visitSegment(advParser.SegmentContext ctx) {
      Boolean res = null;
      return visitChildren(ctx);
      //return res;
   }

   @Override public Boolean visitAllowedAutomaton(advParser.AllowedAutomatonContext ctx) {
      Boolean res = null;
      return visitChildren(ctx);
      //return res;
   }

   @Override public Boolean visitAllowedView(advParser.AllowedViewContext ctx) {
      Boolean res = null;
      return visitChildren(ctx);
      //return res;
   }

   @Override public Boolean visitAllowedAnimation(advParser.AllowedAnimationContext ctx) {
      Boolean res = null;
      return visitChildren(ctx);
      //return res;
   }

   @Override public Boolean visitAllowedViewPort(advParser.AllowedViewPortContext ctx) {
      Boolean res = null;
      return visitChildren(ctx);
      //return res;
   }

   @Override public Boolean visitAllowedForEach(advParser.AllowedForEachContext ctx) {
      Boolean res = null;
      return visitChildren(ctx);
      //return res;
   }

   //-------------------

   @Override public Boolean visitViewPortDefinition(advParser.ViewPortDefinitionContext ctx) {
      Boolean res = true;
      String viewportID = ctx.IDENTIFIER(0).getText();
      String viewID = ctx.IDENTIFIER(1).getText();

      if(currentSymbolTable.contains(viewportID)){
         if(currentSymbolTable.getType(viewportID) == viewType.name())
            ErrorHandling.printError(ctx, "View port '" + viewportID + "' is already defined.");
         else
            ErrorHandling.printError(ctx, "The variable '" + viewportID + "' is already defined.");
         return false;
      }

      if (!currentSymbolTable.contains(viewID)) {
         ErrorHandling.printError(ctx, "View '" + viewID + "' has not been defined.");
         return false;
      }  

      if(res){
         currentSymbolTable.addSymbol(viewportID, viewType);
      }
      res = visit(ctx.point(0)) && visit(ctx.point(1));
      return res;
   }

   @Override public Boolean visitViewPortAccess(advParser.ViewPortAccessContext ctx) {
      Boolean res = true;
      String key = ctx.IDENTIFIER().getText();

      if (!currentSymbolTable.contains(key)) {
         ErrorHandling.printError(ctx, "Viewport '" + key + "' has not been defined.");
         return false;
      }else{
         if (currentSymbolTable.getType(key) != viewType.name()) {
            ErrorHandling.printError(ctx, "Variable '" + key + "' is not of type 'viewport'.");
            return false;
         }
      }
      res = visit(ctx.segment());
      return res;
   }

   @Override public Boolean visitStateProperty(advParser.StatePropertyContext ctx) {
      Boolean res = true;
      String key = ctx.IDENTIFIER().getText();

      if (!currentSymbolTable.contains(key)) {
         ErrorHandling.printError(ctx, "Variable '" + key + "' has not been defined.");
         return false;
      }

      if (strToType(currentSymbolTable.getType(key)) != stateType) {
         ErrorHandling.printError(ctx, "Variable '" + key + "' is not of state type.");
         return false;
      }

      int nElem = ctx.genericList().listElement().size();

      if(nElem > 1){
         ErrorHandling.printError(ctx, "Property change must be a single key = value pair. Found " + ctx.genericList().getText());
         return false;
      }

      String[] args = ctx.genericList().listElement(0).getText().split("=");
      if(!args[0].toLowerCase().equals("initial") && !args[0].toLowerCase().equals("accepting") && !args[0].toLowerCase().equals("highlighted")){
         ErrorHandling.printError(ctx, "Property must be initial, accepting or highlighted. Found " + args[0]);
         return false;
      }

      if(!args[1].toLowerCase().equals("true") && !args[1].toLowerCase().equals("false")){
         ErrorHandling.printError(ctx, "Value must be true or false. Found " + args[1]);
         return false;
      }

      return res;
   }

   @Override public Boolean visitTransitionDefinition(advParser.TransitionDefinitionContext ctx) {
      Boolean res = true;
      List<advParser.TransitionSequenceContext> transitions = ctx.transitionSequence();
      for(int i = 0; i<transitions.size(); i++){
         res = visit(transitions.get(i));
         if(!res){
            break;
         }
      }
      return res;
   }

   @Override public Boolean visitTransitionSequence(advParser.TransitionSequenceContext ctx) {
      Boolean res = true;
      String state1 = ctx.IDENTIFIER(0).getText();
      String state2 = ctx.IDENTIFIER(1).getText();
      List<TerminalNode> symbols = ctx.SYMBOL();

      if(currentAutomatonType.equals("DFA") || currentAutomatonType.equals("complete DFA")){
         if(symbols.size() == 0){
            ErrorHandling.printError(ctx, "DFA automatons can't have empty word transitions.");
            return false;
         }   
      }

      if(!currentSymbolTable.contains(state1)){
         ErrorHandling.printError(ctx, "State '" + state1 + "' has not been defined.");
         return false;
      }

      if(!currentSymbolTable.contains(state2)){
         ErrorHandling.printError(ctx, "State '" + state2 + "' has not been defined.");
         return false;
      }

      Set<String> encounteredSymbols = new HashSet<>();
      for(TerminalNode symbolNode : symbols){
         String symbol = symbolNode.getText();
         if(!alphabetSymbols.contains(symbol)){
            ErrorHandling.printError(ctx, "Symbol " + symbol + " is not part of the alphabet.");
            return false;
         }

         if (encounteredSymbols.contains(symbol)) {
            ErrorHandling.printError(ctx, "Duplicate symbol " + symbol + " found in transition sequence.");
            return false;
         }
         encounteredSymbols.add(symbol);
      }
      return res;
   }

   @Override public Boolean visitTransitionConstruction(advParser.TransitionConstructionContext ctx) {
      Boolean res = true;
      String state1 = ctx.IDENTIFIER(0).getText();
      String state2 = ctx.IDENTIFIER(1).getText();

      SymbolTable automatonTable = globalSymbolTable.getChildren().get(currentAutomaton);

      if(!automatonTable.contains(state1)){
         ErrorHandling.printError(ctx, "State '" + state1 + "' has not been defined.");
         return false;
      }

      if(!automatonTable.contains(state2)){
         ErrorHandling.printError(ctx, "State '" + state2 + "' has not been defined.");
         return false;
      }

      return res;
   }

   @Override public Boolean visitPlaceState(advParser.PlaceStateContext ctx) {
      Boolean res = true;
      List<advParser.StatePlacementContext> statePlacements = ctx.statePlacement();
      for (advParser.StatePlacementContext statePlacement : statePlacements) {
         res = visit(statePlacement);
         if(!res){
            break;
         }
      }
      return true;
   }

   @Override public Boolean visitPlaceTransition(advParser.PlaceTransitionContext ctx) {
      Boolean res = true;
      List<advParser.TransitionPlacementContext> transitionPlacements = ctx.transitionPlacement();
      for (advParser.TransitionPlacementContext transitionPlacement : transitionPlacements) {
         res = visit(transitionPlacement);
         if(!res){
            break;
         }
      }
      return true;
   }

   @Override public Boolean visitStatePlacement(advParser.StatePlacementContext ctx) {
      Boolean res = true;
      String stateID = ctx.IDENTIFIER(0).getText();
      String pointOrIdentifier = ctx.getChild(2).getText();

      SymbolTable automatonTable = globalSymbolTable.getChildren().get(currentAutomaton);

      if (!automatonTable.contains(stateID)) {
         ErrorHandling.printError(ctx, "State '" + stateID + "' has not been defined.");
         return false;
      }

      if (ctx.point() != null) {
         res = visit(ctx.point());
      } else {
         if (!currentSymbolTable.contains(pointOrIdentifier)) {
               ErrorHandling.printError(ctx, "Variable '" + pointOrIdentifier + "' is not defined.");
               return false;
         }
      }
      return res;
   }

   @Override public Boolean visitTransitionPlacement(advParser.TransitionPlacementContext ctx) {
      Boolean res = true;
      String pointOrIdentifier = ctx.getChild(2).getText();

      res = visit(ctx.labelConstruction());

      if (pointOrIdentifier.equals("point")) {
         res = visit(ctx.point());
      } else {
         if (!currentSymbolTable.contains(pointOrIdentifier)) {
               ErrorHandling.printError(ctx, "Variable '" + pointOrIdentifier + "' is not defined.");
               return false;
         }
      }
      return res;
   }

   @Override public Boolean visitLabelConstruction(advParser.LabelConstructionContext ctx) {
      Boolean res = true;

      res = visit(ctx.label());

      if (ctx.genericList() != null) {
         res = visit(ctx.genericList());
      }

      return res;
   }

   @Override public Boolean visitLabel(advParser.LabelContext ctx) {
      Boolean res = true;
    
      String id = ctx.IDENTIFIER().getText();
      if (!id.equals("label")) {
         ErrorHandling.printError(ctx, "Expected 'label' keyword but found: " + id);
         return false;
      }
      
      res = visit(ctx.transitionConstruction());

      return res;
   }

   @Override public Boolean visitArrowProperties(advParser.ArrowPropertiesContext ctx) {
      Boolean res = visit(ctx.transitionConstruction());

      List<TerminalNode> identifiers = ctx.IDENTIFIER();
      List<advParser.GenericListContext> genericLists = ctx.genericList();

      for(TerminalNode identifier : identifiers){
         String id = identifier.getText();
         if(!currentSymbolTable.contains(id)){
            ErrorHandling.printError(ctx, "Variable " + id + " has not been defined.");
            return false;
         }
      }
      
      for(advParser.GenericListContext list : genericLists){
         visit(list);
      }

      return res;
   }

   @Override public Boolean visitShow(advParser.ShowContext ctx) {
      Boolean res = true;
      List<advParser.StatePropertyContext> stateProperties = ctx.stateProperty();
      List<advParser.TransitionConstructionContext> transitionConstructions = ctx.transitionConstruction();
      List<TerminalNode> identifiers = ctx.IDENTIFIER();

      for (advParser.StatePropertyContext stateProperty : stateProperties) {
         res = visit(stateProperty);
      }

      for (advParser.TransitionConstructionContext transitionConstruction : transitionConstructions) {
         res = visit(transitionConstruction);
      }

      for (TerminalNode identifier : identifiers) {
         String id = identifier.getText();
         if (!currentSymbolTable.contains(id)) {
             ErrorHandling.printError(ctx, "Variable '" + id + "' has not been defined.");
             return false;
         }
      }
      return res;
   }

   @Override public Boolean visitGridConstruction(advParser.GridConstructionContext ctx) {
      String identifier = ctx.IDENTIFIER().getText();

      if (currentSymbolTable.contains(identifier)) {
         ErrorHandling.printError(ctx, "Variable '" + identifier + "' is already defined.");
         return false;
      }

      Boolean res = visit(ctx.point());
      if (!res) {
         return false;
      }
      
      int nElem = ctx.genericList().listElement().size();

      if(nElem < 4){
         ErrorHandling.printError(ctx, "Invalid format in grid construction: 4 pairs of key=value expected.");
         return false;
      }

      for (int i = 0; i < nElem; i++) {
         String[] keyValue = ctx.genericList().listElement(i).getText().split("=");
         

         if (keyValue.length != 2) {
            ErrorHandling.printError(ctx, "Invalid format in genericList: key=value expected.");
            return false;
         }

         String key = keyValue[0].toLowerCase();
         String value = keyValue[1];
         
         Double step = 0.0, margin = 0.0;

         switch (key) {
            case "step":
                  try {
                     step = Double.parseDouble(value);
                  } catch (NumberFormatException e) {
                     ErrorHandling.printError(ctx, "Invalid value for 'step': a number (integer or float) expected.");
                  }
                  break;

            case "margin":
                  try {
                     margin = Double.parseDouble(value);
                  } catch (NumberFormatException e) {
                     ErrorHandling.printError(ctx, "Invalid value for 'step': a number (integer or float) expected.");
                  }
               if (margin.doubleValue() >= step.doubleValue()) {
                  System.out.println(margin.doubleValue());
                  System.out.println(step.doubleValue());
                  ErrorHandling.printError(ctx, "Invalid value for 'margin': must be lower than 'step'.");
                  return false;
               }
               break;

            case "color":
                  if (!value.equals("red") || !value.equals("green") || !value.equals("blue") || !value.equals("gray")) {
                        ErrorHandling.printError(ctx, "Invalid value for 'color': must be 'red', 'green', 'blue', or 'gray'.");
                        return false;
                  }
                  break;

            case "line":
               if (!value.equals("solid") || !value.equals("dotted") || !value.equals("dashed")) {
                  ErrorHandling.printError(ctx, "Invalid value for 'line': must be 'solid', 'dotted', or 'dashed'.");
                  return false;
               }
               break;

            default:
               ErrorHandling.printError(ctx, "Unknown key: '" + key + "'.");
               return false;
         }
      }

      currentSymbolTable.addSymbol(identifier, gridType);
      return res;
   }

   @Override public Boolean visitPause(advParser.PauseContext ctx) {
      if (ctx.IDENTIFIER() != null) {
         String identifier = ctx.IDENTIFIER().getText();
         if (!currentSymbolTable.contains(identifier)) {
            ErrorHandling.printError(ctx, "Animation '" + identifier + "' has not been defined.");
            return false;
         }
     }
     return true;
   }

   @Override public Boolean visitPlay(advParser.PlayContext ctx) {
      String identifier = ctx.IDENTIFIER().getText();
      if (!currentSymbolTable.contains(identifier)) {
         ErrorHandling.printError(ctx, "Animation '" + identifier + "' has not been defined.");
         return false;
      }
     return true;
   }

   @Override public Boolean visitReadInput(advParser.ReadInputContext ctx) {
      Boolean res = null;
      return visitChildren(ctx);
      //return res;
   }

   @Override public Boolean visitForEachListSegment(advParser.ForEachListSegmentContext ctx) {
      Boolean res = true;
      String identifier = ctx.IDENTIFIER(0).getText();
    
      if (currentSymbolTable.contains(identifier)) {
         ErrorHandling.printError(ctx, "Variable '" + identifier + "' is already defined.");
         return false;
      }

      if(ctx.genericList() != null){
         res = visit(ctx.genericList());
         if (!res) {
            return false;
         }
      }

      if(ctx.IDENTIFIER(1) != null){
         if (!currentSymbolTable.contains(identifier)) {
            ErrorHandling.printError(ctx, "Variable '" + identifier + "' is not defined.");
            return false;
         }
      }
      
      res = visit(ctx.segment());
      if (!res) {
         return false;
      }
      
      return res;
   }

   @Override public Boolean visitForEachList(advParser.ForEachListContext ctx) {
      Boolean res = true;
      String identifier = ctx.IDENTIFIER(0).getText();
    
      if (currentSymbolTable.contains(identifier)) {
         ErrorHandling.printError(ctx, "Variable '" + identifier + "' is already defined.");
         return false;
      }
      
      if(ctx.genericList() != null){
         res = visit(ctx.genericList());
         if (!res) {
            return false;
         }
      }

      if(ctx.IDENTIFIER(1) != null){
         if (!currentSymbolTable.contains(identifier)) {
            ErrorHandling.printError(ctx, "Variable '" + identifier + "' is not defined.");
            return false;
         }
      }
      
      res = visit(ctx.allowedForEach());
      if (!res) {
         return false;
      }
      
      return res;
   }

   @Override public Boolean visitForEachSetSegment(advParser.ForEachSetSegmentContext ctx) {
      String identifier = ctx.IDENTIFIER().getText();
    
      if (currentSymbolTable.contains(identifier)) {
         ErrorHandling.printError(ctx, "Variable '" + identifier + "' is already defined.");
         return false;
      }
      
      Boolean res = visit(ctx.genericSet());
      if (!res) {
         return false;
      }
      
      res = visit(ctx.segment());
      if (!res) {
         return false;
      }
      
      return res;
   }

   @Override public Boolean visitForEachSet(advParser.ForEachSetContext ctx) {
      String identifier = ctx.IDENTIFIER().getText();
    
      if (currentSymbolTable.contains(identifier)) {
         ErrorHandling.printError(ctx, "Variable '" + identifier + "' is already defined.");
         return false;
      }
      
      Boolean res = visit(ctx.genericSet());
      if (!res) {
         return false;
      }
      
      res = visit(ctx.allowedForEach());
      if (!res) {
         return false;
      }
      
      return res;
   }

   @Override public Boolean visitConditional(advParser.ConditionalContext ctx) {
      Boolean res = visit(ctx.expression());

      if (res && ctx.expression().t != booleanType) {
         ErrorHandling.printError(ctx, "Expression in if statement must have boolean type, but found '" + ctx.expression().t + "'.");
         return false;
      }

      return visitChildren(ctx);
   }

   @Override public Boolean visitWhileLoop(advParser.WhileLoopContext ctx) {
      Boolean res = visit(ctx.expression());

      if (res && ctx.expression().t != booleanType) {
         ErrorHandling.printError(ctx, "Expression in while statement must have boolean type, but found '" + ctx.expression().t + "'.");
         return false;
      }

      return visitChildren(ctx);
   }

   @Override public Boolean visitBreakLoop(advParser.BreakLoopContext ctx) {
      Boolean res = null;
      return visitChildren(ctx);
      //return res;
   }

   @Override public Boolean visitContinueLoop(advParser.ContinueLoopContext ctx) {
      Boolean res = null;
      return visitChildren(ctx);
      //return res;
   }

   @Override public Boolean visitGenericList(advParser.GenericListContext ctx) {
      return visitChildren(ctx);
   }

   @Override public Boolean visitListElement(advParser.ListElementContext ctx) {
      /* 
      // Left side
      if(ctx.getChild(0).getSymbol().getType() == advLexer.point || ctx.getChild(0).getSymbol().getType() == advLexer.number){
         res = visit(ctx.getChild(0)); 
      } else {
         if(ctx.getChild(0).getSymbol().getType() == advLexer.IDENTIFIER){
            String key = ctx.IDENTIFIER().getText();
            if(!currentSymbolTable.containsLocally(key)){
               res = false;
            }else{
               ctx.t = currentSymbolTable.getType(key);
            }
         } else if (ctx.getChild(0).getSymbol().getType() == advLexer.string) {
            ctx.t = stringType;
            res = true;
         }
      }

      // Right side
      if(ctx.getChild(2).getSymbol().getType() == advLexer.point || ctx.getChild(2).getSymbol().getType() == advLexer.number){
         res = visit(ctx.getChild(2)); 
      } else {
         if(ctx.getChild(2).getSymbol().getType() == advLexer.IDENTIFIER){
            String key = ctx.IDENTIFIER().getText();
            if(!currentSymbolTable.containsLocally(key)){
               res = false;
            }else{
               ctx.t = currentSymbolTable.getType(key);
            }
         } else if (ctx.getChild(2).getSymbol().getType() == advLexer.STRING) {
            ctx.t = stringType;
            res = true;
         } else if (ctx.getChild(2).getSymbol().getType() == advLexer.BOOLEAN) {
            ctx.t = booleanType;
            res = true;
         }
      }
      
     return res;
     */
      return true;
   }

   @Override public Boolean visitGenericSet(advParser.GenericSetContext ctx) {
      return visitChildren(ctx);
   }

   @Override public Boolean visitSetElement(advParser.SetElementContext ctx) {
      Boolean res = true;
      if (ctx.point() != null) {
         res = visit(ctx.point());                   // Visit the 'point' non-terminal node
      } else if (ctx.number() != null) {
         res = visit(ctx.number());                  // Visit the 'number' non-terminal node
      } else if (ctx.IDENTIFIER() != null) {
         String key = ctx.IDENTIFIER().getText();
         if(!currentSymbolTable.containsLocally(key)){
            res = false;
         }else{
            //ctx.t = strToType(currentSymbolTable.getType(key));
         }
      } else if (ctx.STRING() != null) {
         //ctx.t = stringType;
      }
      return res;
   }

   @Override public Boolean visitNumber(advParser.NumberContext ctx) {
      if(ctx.INTEGER() != null)
         ctx.t = integerType;
      else if(ctx.FLOAT() != null)
         ctx.t = floatType;
      return true;
   }

   @Override public Boolean visitPoint(advParser.PointContext ctx) {
      Boolean res = visit(ctx.x) && visit(ctx.y);
      if(res){
         ctx.t = pointType;
      }else{
         ErrorHandling.printError(ctx, "Invalid point: coordinates must be of numeric types.");
         res = false;
   }
      return res;
   }

   @Override public Boolean visitCondLoopStats(advParser.CondLoopStatsContext ctx) {
      return visitChildren(ctx);
   }

   @Override public Boolean visitPrint(advParser.PrintContext ctx) {
      return visit(ctx.expression());
   }

   private Type getResultType(Type t1, Type t2, String op) {

      if(t1 != null && t2 != null){

         //Numbers
         if (t1.name().equals("integer") && t2.name().equals("integer")) {
            if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") || op.equals("%") || op.equals("^")) {
               return integerType;
            }
         } else if (t1.name().equals("float") && t2.name().equals("float")) {
            if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/")) {
               return floatType;
            }
         } else if ((t1.name().equals("integer") && t2.name().equals("float")) || (t1.name().equals("float") && t2.name().equals("integer"))) {
            if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/")) {
               return floatType;
            }
         } else if (t1.name().equals("boolean") && t2.name().equals("boolean")) {
            if (op.equals("&&") || op.equals("||")) {
               return booleanType;
            }

         //Points
         } else if (t1.name().equals("point") && t2.name().equals("point")) {
            if (op.equals("+") || op.equals("-")) {
               return pointType;
            }
         } else if ((t1.name().equals("integer") || t1.name().equals("float")) && t2.name().equals("point")) {
            if (op.equals("+") || op.equals("-") || op.equals("*")) {
               return pointType;
            }
         } else if (t1.name().equals("point") && (t2.name().equals("integer") || t2.name().equals("float"))) {
            if (op.equals("+") || op.equals("-") || op.equals("*")) {
               return pointType;
            }
         } else if (t1.name().equals("point") && (t2.name().equals("integer") || t2.name().equals("float"))) {
            if (op.equals("/")) {
               return pointType;
            }

         //Conditional
         }else if ((t1.name().equals("integer") || t1.name().equals("float")) && (t2.name().equals("integer") || t2.name().equals("float"))) {
            if (op.equals("==") || op.equals("!=") || op.equals("<") || op.equals(">") || op.equals(">=") || op.equals("<=")) {
               return booleanType;
            }
         } else if (t1.name().equals("point") && t2.name().equals("point")) {
            if (op.equals("==") || op.equals("!=")) {
               return booleanType;
            }
         } else if (t1.name().equals("boolean") && t2.name().equals("boolean")) {
            if (op.equals("==") || op.equals("!=")) {
               return booleanType;
            }   
         }
      }
      return null;
   }

   private boolean isValidPrefixExpression(Type t, String op) {
   if (op.equals("+") || op.equals("-")) {
      return t.name().equals("integer") || t.name().equals("float") || t.name().equals("point");
   } else if (op.equals("!")) {
      return t.name().equals("boolean");
   }
   return false;
   }

   private Type strToType(String str){
      Type type = null;
      if(str != null){
         switch (str) {
            case "boolean":
               type = integerType;
               break;

            case "integer":
               type = integerType;
               break;

            case "float":
               type = floatType;
               break;
            
            case "string":
               type = stringType;
               break;

            case "symbol":
               type = symbolType;
               break;

            case "list":
               type = listType;
               break;

            case "set":
               type = setType;
               break;
            
            case "number":
               type = numberType;
               break;

            case "point":
               type = pointType;
               break;

            case "state":
               type = stateType;
               break;

            default:
               break;
         }
      }
      return type;
   }
}