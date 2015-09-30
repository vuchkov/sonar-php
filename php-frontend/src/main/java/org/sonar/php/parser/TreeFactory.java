/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.parser;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sonar.sslr.api.typed.Optional;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.tree.impl.CompilationUnitTreeImpl;
import org.sonar.php.tree.impl.ScriptTreeImpl;
import org.sonar.php.tree.impl.SeparatedList;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.tree.impl.declaration.ClassDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.ClassPropertyDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.ConstantDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.FunctionDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.MethodDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.NamespaceNameTreeImpl;
import org.sonar.php.tree.impl.declaration.ParameterListTreeImpl;
import org.sonar.php.tree.impl.declaration.ParameterTreeImpl;
import org.sonar.php.tree.impl.declaration.TraitAliasTreeImpl;
import org.sonar.php.tree.impl.declaration.TraitMethodReferenceTreeImpl;
import org.sonar.php.tree.impl.declaration.TraitPrecedenceTreeImpl;
import org.sonar.php.tree.impl.declaration.UseTraitDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.UseClauseTreeImpl;
import org.sonar.php.tree.impl.expression.ArrayAccessTreeImpl;
import org.sonar.php.tree.impl.expression.ArrayInitializerBracketTreeImpl;
import org.sonar.php.tree.impl.expression.ArrayInitializerFunctionTreeImpl;
import org.sonar.php.tree.impl.expression.ArrayPairTreeImpl;
import org.sonar.php.tree.impl.expression.AssignmentByReferenceTreeImpl;
import org.sonar.php.tree.impl.expression.AssignmentExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.BinaryExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.CastExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.CompoundVariableTreeImpl;
import org.sonar.php.tree.impl.expression.ComputedVariableTreeImpl;
import org.sonar.php.tree.impl.expression.ConditionalExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.ExitTreeImpl;
import org.sonar.php.tree.impl.expression.ExpandableStringCharactersTreeImpl;
import org.sonar.php.tree.impl.expression.ExpandableStringLiteralTreeImpl;
import org.sonar.php.tree.impl.expression.FunctionCallTreeImpl;
import org.sonar.php.tree.impl.expression.FunctionExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.IdentifierTreeImpl;
import org.sonar.php.tree.impl.expression.LexicalVariablesTreeImpl;
import org.sonar.php.tree.impl.expression.ListExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.LiteralTreeImpl;
import org.sonar.php.tree.impl.expression.MemberAccessTreeImpl;
import org.sonar.php.tree.impl.expression.NewExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.ParenthesizedExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.PostfixExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.ReferenceVariableTreeImpl;
import org.sonar.php.tree.impl.expression.SpreadArgumentTreeImpl;
import org.sonar.php.tree.impl.expression.PrefixExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.VariableVariableTreeImpl;
import org.sonar.php.tree.impl.expression.YieldExpressionTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.impl.statement.BlockTreeImpl;
import org.sonar.php.tree.impl.statement.BreakStatementTreeImpl;
import org.sonar.php.tree.impl.statement.CaseClauseTreeImpl;
import org.sonar.php.tree.impl.statement.CatchBlockTreeImpl;
import org.sonar.php.tree.impl.statement.ContinueStatementTreeImpl;
import org.sonar.php.tree.impl.statement.DeclareStatementTreeImpl;
import org.sonar.php.tree.impl.statement.DeclareStatementTreeImpl.DeclareStatementHead;
import org.sonar.php.tree.impl.statement.DefaultClauseTreeImpl;
import org.sonar.php.tree.impl.statement.DoWhileStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ElseClauseTreeImpl;
import org.sonar.php.tree.impl.statement.ElseifClauseTreeImpl;
import org.sonar.php.tree.impl.statement.EmptyStatementImpl;
import org.sonar.php.tree.impl.statement.ExpressionStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ForEachStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ForEachStatementTreeImpl.ForEachStatementHeader;
import org.sonar.php.tree.impl.statement.ForStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ForStatementTreeImpl.ForStatementHeader;
import org.sonar.php.tree.impl.statement.GlobalStatementTreeImpl;
import org.sonar.php.tree.impl.statement.GotoStatementTreeImpl;
import org.sonar.php.tree.impl.statement.IfStatementTreeImpl;
import org.sonar.php.tree.impl.statement.InlineHTMLTreeImpl;
import org.sonar.php.tree.impl.statement.LabelTreeImpl;
import org.sonar.php.tree.impl.statement.NamespaceStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ReturnStatementTreeImpl;
import org.sonar.php.tree.impl.statement.StaticStatementTreeImpl;
import org.sonar.php.tree.impl.statement.SwitchStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ThrowStatementTreeImpl;
import org.sonar.php.tree.impl.statement.TryStatementImpl;
import org.sonar.php.tree.impl.statement.UnsetVariableStatementTreeImpl;
import org.sonar.php.tree.impl.statement.UseStatementTreeImpl;
import org.sonar.php.tree.impl.statement.VariableDeclarationTreeImpl;
import org.sonar.php.tree.impl.statement.WhileStatementTreeImpl;
import org.sonar.php.tree.impl.statement.YieldStatementTreeImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ConstantDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentByReferenceTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.CompoundVariableTree;
import org.sonar.plugins.php.api.tree.expression.ComputedVariableTree;
import org.sonar.plugins.php.api.tree.expression.ExitTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.expression.ListExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ReferenceVariableTree;
import org.sonar.plugins.php.api.tree.expression.SpreadArgumentTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.expression.YieldExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.CaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;
import org.sonar.plugins.php.api.tree.statement.DeclareStatementTree;
import org.sonar.plugins.php.api.tree.statement.DefaultClauseTree;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.EmptyStatementTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.GlobalStatementTree;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.InlineHTMLTree;
import org.sonar.plugins.php.api.tree.statement.LabelTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.StaticStatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.TraitAdaptationStatementTree;
import org.sonar.plugins.php.api.tree.statement.TraitAliasTree;
import org.sonar.plugins.php.api.tree.statement.TraitMethodReferenceTree;
import org.sonar.plugins.php.api.tree.statement.TraitPrecedenceTree;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.tree.statement.UnsetVariableStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.YieldStatementTree;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class TreeFactory {

  private static final Map<String, Kind> BINARY_EXPRESSION_KINDS_BY_OPERATOR = ImmutableMap.<String, Kind>builder()
    .put(PHPPunctuator.DOT.getValue(), Kind.CONCATENATION)
    .put(PHPPunctuator.STAR.getValue(), Kind.MULTIPLY)
    .put(PHPPunctuator.DIV.getValue(), Kind.DIVIDE)
    .put(PHPPunctuator.MOD.getValue(), Kind.REMAINDER)
    .put(PHPPunctuator.PLUS.getValue(), Kind.PLUS)
    .put(PHPPunctuator.MINUS.getValue(), Kind.MINUS)
    .put(PHPPunctuator.SL.getValue(), Kind.LEFT_SHIFT)
    .put(PHPPunctuator.SR.getValue(), Kind.RIGHT_SHIFT)
    .put(PHPPunctuator.LT.getValue(), Kind.LESS_THAN)
    .put(PHPPunctuator.GT.getValue(), Kind.GREATER_THAN)
    .put(PHPPunctuator.LE.getValue(), Kind.LESS_THAN_OR_EQUAL_TO)
    .put(PHPPunctuator.GE.getValue(), Kind.GREATER_THAN_OR_EQUAL_TO)
    .put(PHPPunctuator.EQUAL.getValue(), Kind.EQUAL_TO)
    .put(PHPPunctuator.EQUAL2.getValue(), Kind.STRICT_EQUAL_TO)
    .put(PHPPunctuator.NOTEQUAL.getValue(), Kind.NOT_EQUAL_TO)
    .put(PHPPunctuator.NOTEQUAL2.getValue(), Kind.STRICT_NOT_EQUAL_TO)
    .put(PHPPunctuator.NOTEQUALBIS.getValue(), Kind.ALTERNATIVE_NOT_EQUAL_TO)
    .put(PHPPunctuator.AMPERSAND.getValue(), Kind.BITWISE_AND)
    .put(PHPPunctuator.XOR.getValue(), Kind.BITWISE_XOR)
    .put(PHPPunctuator.OR.getValue(), Kind.BITWISE_OR)
    .put(PHPPunctuator.ANDAND.getValue(), Kind.CONDITIONAL_AND)
    .put(PHPPunctuator.OROR.getValue(), Kind.CONDITIONAL_OR)
    .put(PHPKeyword.AND.getValue(), Kind.ALTERNATIVE_CONDITIONAL_AND)
    .put(PHPKeyword.XOR.getValue(), Kind.ALTERNATIVE_CONDITIONAL_XOR)
    .put(PHPKeyword.OR.getValue(), Kind.ALTERNATIVE_CONDITIONAL_OR)
    .build();

  private static final Map<String, Kind> UNARY_EXPRESSION_KINDS_BY_OPERATOR = ImmutableMap.<String, Kind>builder()
    .put(PHPPunctuator.INC.getValue(), Kind.PREFIX_INCREMENT)
    .put(PHPPunctuator.DEC.getValue(), Kind.PREFIX_DECREMENT)
    .put(PHPPunctuator.PLUS.getValue(), Kind.UNARY_PLUS)
    .put(PHPPunctuator.MINUS.getValue(), Kind.UNARY_MINUS)
    .put(PHPPunctuator.TILDA.getValue(), Kind.BITWISE_COMPLEMENT)
    .put(PHPPunctuator.BANG.getValue(), Kind.LOGICAL_COMPLEMENT)
    .put(PHPPunctuator.AT.getValue(), Kind.ERROR_CONTROL)
    .build();

  private static <T extends Tree> List<T> optionalList(Optional<List<T>> list) {
    if (list.isPresent()) {
      return list.get();
    } else {
      return Collections.emptyList();
    }
  }

  private static <T extends Tree> SeparatedList<T> optionalSeparatedList(Optional<SeparatedList<T>> list) {
    if (list.isPresent()) {
      return list.get();
    } else {
      return new SeparatedList<>(new LinkedList<T>(), new LinkedList<InternalSyntaxToken>());
    }
  }

  private <T extends Tree> SeparatedList<T> separatedList(T firstElement, Optional<List<Tuple<InternalSyntaxToken, T>>> tuples) {
    return separatedList(firstElement, tuples, null);
  }

  private <T extends Tree> SeparatedList<T> separatedList(T firstElement, Optional<List<Tuple<InternalSyntaxToken, T>>> tuples, InternalSyntaxToken trailingSeparator) {
    ImmutableList.Builder<T> elements = ImmutableList.builder();
    ImmutableList.Builder<InternalSyntaxToken> separators = ImmutableList.builder();

    elements.add(firstElement);
    if (tuples.isPresent()) {
      for (Tuple<InternalSyntaxToken, T> tuple : tuples.get()) {
        separators.add(tuple.first());
        elements.add(tuple.second());
      }
    }

    if (trailingSeparator != null) {
      separators.add(trailingSeparator);
    }

    return new SeparatedList<>(elements.build(), separators.build());
  }


  public ScriptTree script(InternalSyntaxToken fileOpeningTagToken, Optional<List<StatementTree>> statements) {
    return new ScriptTreeImpl(fileOpeningTagToken, optionalList(statements));
  }

  public CompilationUnitTree compilationUnit(Optional<ScriptTree> script, InternalSyntaxToken eofToken) {
    return new CompilationUnitTreeImpl(script.orNull(), eofToken);
  }

  /**
   * [ START ] Declarations
   */

  public VariableDeclarationTree variableDeclaration(InternalSyntaxToken identifierToken, Optional<Tuple<InternalSyntaxToken, ExpressionTree>> optionalEqual) {
    if (optionalEqual.isPresent()) {
      return new VariableDeclarationTreeImpl(new IdentifierTreeImpl(identifierToken), optionalEqual.get().first(), optionalEqual.get().second());
    } else {
      return new VariableDeclarationTreeImpl(new IdentifierTreeImpl(identifierToken), null, null);
    }
  }

  public VariableDeclarationTree staticVar(InternalSyntaxToken identifierToken, Optional<Tuple<InternalSyntaxToken, ExpressionTree>> optionalEqual) {
    return variableDeclaration(identifierToken, optionalEqual);
  }

  public VariableDeclarationTree memberConstDeclaration(InternalSyntaxToken identifierToken, Optional<Tuple<InternalSyntaxToken, ExpressionTree>> optionalEqual) {
    return variableDeclaration(identifierToken, optionalEqual);
  }

  public VariableDeclarationTree constDeclaration(InternalSyntaxToken identifierToken, InternalSyntaxToken equToken, ExpressionTree expression) {
    return new VariableDeclarationTreeImpl(new IdentifierTreeImpl(identifierToken), equToken, expression);
  }

  public UseClauseTree useClause(NamespaceNameTree namespaceName, Optional<Tuple<InternalSyntaxToken, InternalSyntaxToken>> alias) {
    if (alias.isPresent()) {
      IdentifierTreeImpl aliasName = new IdentifierTreeImpl(alias.get().second());
      return new UseClauseTreeImpl(namespaceName, alias.get().first(), aliasName);
    }
    return new UseClauseTreeImpl(namespaceName);
  }

  public ClassPropertyDeclarationTree classConstantDeclaration(
    InternalSyntaxToken constToken,
    VariableDeclarationTree firstDeclaration,
    Optional<List<Tuple<InternalSyntaxToken, VariableDeclarationTree>>> additionalDeclarations,
    InternalSyntaxToken eosToken
  ) {
    return ClassPropertyDeclarationTreeImpl.constant(constToken, separatedList(firstDeclaration, additionalDeclarations), eosToken);
  }

  public ConstantDeclarationTree constantDeclaration(
    InternalSyntaxToken constToken,
    VariableDeclarationTree firstDeclaration,
    Optional<List<Tuple<InternalSyntaxToken, VariableDeclarationTree>>> additionalDeclarations,
    InternalSyntaxToken eosToken
  ) {
    return new ConstantDeclarationTreeImpl(constToken, separatedList(firstDeclaration, additionalDeclarations), eosToken);
  }

  public ClassPropertyDeclarationTree classVariableDeclaration(
    List<SyntaxToken> modifierTokens,
    VariableDeclarationTree firstVariable,
    Optional<List<Tuple<InternalSyntaxToken, VariableDeclarationTree>>> additionalVariables,
    InternalSyntaxToken eosToken
  ) {
    return ClassPropertyDeclarationTreeImpl.variable(modifierTokens, separatedList(firstVariable, additionalVariables), eosToken);
  }

  public MethodDeclarationTree methodDeclaration(
    Optional<List<SyntaxToken>> modifiers,
    InternalSyntaxToken functionToken,
    Optional<InternalSyntaxToken> referenceToken,
    IdentifierTree name,
    ParameterListTree parameters,
    Tree body
  ) {
    return new MethodDeclarationTreeImpl(optionalList(modifiers), functionToken, referenceToken.orNull(), name, parameters, body);
  }

  public FunctionDeclarationTree functionDeclaration(
    InternalSyntaxToken functionToken,
    Optional<InternalSyntaxToken> referenceToken,
    IdentifierTree name,
    ParameterListTree parameters,
    BlockTree body
  ) {
    return new FunctionDeclarationTreeImpl(functionToken, referenceToken.orNull(), name, parameters, body);
  }

  public ParameterListTree parameterList(
    InternalSyntaxToken leftParenthesis,
    Optional<Tuple<ParameterTree, Optional<List<Tuple<InternalSyntaxToken, ParameterTree>>>>> parameters,
    InternalSyntaxToken rightParenthesis
  ) {
    SeparatedList<ParameterTree> separatedList = SeparatedList.empty();
    if (parameters.isPresent()) {
      separatedList = separatedList(parameters.get().first(), parameters.get().second());
    }
    return new ParameterListTreeImpl(leftParenthesis, separatedList, rightParenthesis);
  }

  public ParameterTree parameter(
    Optional<Tree> classType,
    Optional<InternalSyntaxToken> ampersand,
    Optional<InternalSyntaxToken> ellipsis,
    InternalSyntaxToken identifier,
    Optional<Tuple<InternalSyntaxToken, ExpressionTree>> eqAndInitValue
  ) {
    InternalSyntaxToken eqToken = null;
    ExpressionTree initValue = null;
    if (eqAndInitValue.isPresent()) {
      eqToken = eqAndInitValue.get().first();
      initValue = eqAndInitValue.get().second();
    }
    VariableIdentifierTree varIdentifier = new VariableIdentifierTreeImpl(new IdentifierTreeImpl(identifier));
    return new ParameterTreeImpl(classType.orNull(), ampersand.orNull(), ellipsis.orNull(), varIdentifier, eqToken, initValue);
  }

  public SeparatedList<NamespaceNameTree> interfaceList(NamespaceNameTree first, Optional<List<Tuple<InternalSyntaxToken, NamespaceNameTree>>> others) {
    return separatedList(first, others);
  }

  public UseTraitDeclarationTree useTraitDeclaration(InternalSyntaxToken useToken, SeparatedList<NamespaceNameTree> traits, InternalSyntaxToken eosToken) {
    return new UseTraitDeclarationTreeImpl(useToken, traits, eosToken);
  }

  public UseTraitDeclarationTree useTraitDeclaration(
    InternalSyntaxToken useToken,
    SeparatedList<NamespaceNameTree> traits,
    InternalSyntaxToken openCurlyBrace,
    Optional<List<TraitAdaptationStatementTree>> adaptations,
    InternalSyntaxToken closeCurlyBrace
  ) {
    return new UseTraitDeclarationTreeImpl(useToken, traits, openCurlyBrace, optionalList(adaptations), closeCurlyBrace);
  }

  public TraitPrecedenceTree traitPrecedence(
    TraitMethodReferenceTree methodReference,
    InternalSyntaxToken insteadOfToken,
    SeparatedList<NamespaceNameTree> traits,
    InternalSyntaxToken eosToken
  ) {
    return new TraitPrecedenceTreeImpl(methodReference, insteadOfToken, traits, eosToken);
  }

  public TraitAliasTree traitAlias(
    TraitMethodReferenceTree methodReference,
    InternalSyntaxToken asToken,
    Optional<SyntaxToken> modifier,
    IdentifierTree alias,
    InternalSyntaxToken eos
  ) {
    return new TraitAliasTreeImpl(methodReference, asToken, modifier.orNull(), alias, eos);
  }

  public TraitAliasTree traitAlias(
    TraitMethodReferenceTree methodReference,
    InternalSyntaxToken asToken,
    SyntaxToken modifier,
    InternalSyntaxToken eos
  ) {
    return new TraitAliasTreeImpl(methodReference, asToken, modifier, null, eos);
  }

  public TraitMethodReferenceTree traitMethodReference(InternalSyntaxToken identifier) {
    return new TraitMethodReferenceTreeImpl(new IdentifierTreeImpl(identifier));
  }

  public TraitMethodReferenceTree traitMethodReference(NamespaceNameTree trait, InternalSyntaxToken doubleColonToken, InternalSyntaxToken identifier) {
    return new TraitMethodReferenceTreeImpl(trait, doubleColonToken, new IdentifierTreeImpl(identifier));
  }

  public ClassDeclarationTree interfaceDeclaration(
    InternalSyntaxToken interfaceToken, IdentifierTree name,
    Optional<Tuple<InternalSyntaxToken, SeparatedList<NamespaceNameTree>>> extendsClause,
    InternalSyntaxToken openCurlyBraceToken, Optional<List<ClassMemberTree>> members, InternalSyntaxToken closeCurlyBraceToken
  ) {
    InternalSyntaxToken extendsToken = null;
    SeparatedList<NamespaceNameTree> interfaceList = SeparatedList.empty();
    if (extendsClause.isPresent()) {
      extendsToken = extendsClause.get().first();
      interfaceList = extendsClause.get().second();
    }
    return ClassDeclarationTreeImpl.createInterface(
      interfaceToken,
      name,
      extendsToken,
      interfaceList,
      openCurlyBraceToken,
      optionalList(members),
      closeCurlyBraceToken
    );
  }

  public ClassDeclarationTree traitDeclaration(
    InternalSyntaxToken traitToken, IdentifierTree name,
    InternalSyntaxToken openCurlyBraceToken, Optional<List<ClassMemberTree>> members, InternalSyntaxToken closeCurlyBraceToken
  ) {
    return ClassDeclarationTreeImpl.createTrait(
      traitToken,
      name,
      openCurlyBraceToken,
      optionalList(members),
      closeCurlyBraceToken
    );
  }

  public ClassDeclarationTree classDeclaration(
    Optional<InternalSyntaxToken> modifier, InternalSyntaxToken classToken, IdentifierTree name,
    Optional<Tuple<InternalSyntaxToken, NamespaceNameTree>> extendsClause,
    Optional<Tuple<InternalSyntaxToken, SeparatedList<NamespaceNameTree>>> implementsClause,
    InternalSyntaxToken openCurlyBrace, Optional<List<ClassMemberTree>> members, InternalSyntaxToken closeCurlyBrace
  ) {
    InternalSyntaxToken extendsToken = null;
    NamespaceNameTree superClass = null;

    InternalSyntaxToken implementsToken = null;
    SeparatedList<NamespaceNameTree> superInterfaces = SeparatedList.empty();

    if (extendsClause.isPresent()) {
      extendsToken = extendsClause.get().first();
      superClass = extendsClause.get().second();
    }

    if (implementsClause.isPresent()) {
      implementsToken = implementsClause.get().first();
      superInterfaces = implementsClause.get().second();
    }

    return ClassDeclarationTreeImpl.createClass(
      modifier.orNull(), classToken, name,
      extendsToken, superClass,
      implementsToken, superInterfaces,
      openCurlyBrace, optionalList(members), closeCurlyBrace
    );
  }

  /**
   * [ END ] Declarations
   */


  /**
   * [ START ] Statement
   */

  public GlobalStatementTree globalStatement(
    InternalSyntaxToken globalToken, VariableTree variable,
    Optional<List<Tuple<InternalSyntaxToken, VariableTree>>> variableRest, InternalSyntaxToken eosToken
  ) {
    return new GlobalStatementTreeImpl(
      globalToken,
      separatedList(variable, variableRest),
      eosToken
    );
  }

  public VariableTree globalVar(Optional<List<InternalSyntaxToken>> dollars, VariableTree variableTree) {
    if (dollars.isPresent()) {
      return new VariableVariableTreeImpl(dollars.get(), variableTree);
    }
    return variableTree;
  }

  public UseStatementTree useStatement(
    InternalSyntaxToken useToken,
    Optional<InternalSyntaxToken> useTypeToken,
    UseClauseTree firstDeclaration,
    Optional<List<Tuple<InternalSyntaxToken, UseClauseTree>>> additionalDeclarations,
    InternalSyntaxToken eosToken
  ) {
    SeparatedList<UseClauseTree> declarations = separatedList(firstDeclaration, additionalDeclarations);
    return new UseStatementTreeImpl(useToken, useTypeToken.orNull(), declarations, eosToken);
  }

  public ReturnStatementTree returnStatement(InternalSyntaxToken returnToken, Optional<ExpressionTree> expression, InternalSyntaxToken eos) {
    return new ReturnStatementTreeImpl(returnToken, expression.orNull(), eos);
  }

  public ContinueStatementTree continueStatement(InternalSyntaxToken continueToken, Optional<ExpressionTree> expression, InternalSyntaxToken eos) {
    return new ContinueStatementTreeImpl(continueToken, expression.orNull(), eos);
  }

  public BreakStatementTree breakStatement(InternalSyntaxToken breakToken, Optional<ExpressionTree> expression, InternalSyntaxToken eos) {
    return new BreakStatementTreeImpl(breakToken, expression.orNull(), eos);
  }

  public BlockTree block(InternalSyntaxToken lbrace, Optional<List<StatementTree>> statements, InternalSyntaxToken rbrace) {
    return new BlockTreeImpl(lbrace, optionalList(statements), rbrace);
  }

  public GotoStatementTree gotoStatement(InternalSyntaxToken gotoToken, InternalSyntaxToken identifier, InternalSyntaxToken eos) {
    return new GotoStatementTreeImpl(gotoToken, new IdentifierTreeImpl(identifier), eos);
  }

  public ExpressionStatementTree expressionStatement(ExpressionTree expression, InternalSyntaxToken eos) {
    return new ExpressionStatementTreeImpl(expression, eos);
  }

  public LabelTree label(InternalSyntaxToken identifier, InternalSyntaxToken colon) {
    return new LabelTreeImpl(new IdentifierTreeImpl(identifier), colon);
  }


  public TryStatementTree tryStatement(
    InternalSyntaxToken tryToken, BlockTree blockTree,
    Optional<List<CatchBlockTree>> catchBlocks,
    Optional<Tuple<InternalSyntaxToken, BlockTree>> finallyBlock
  ) {
    if (finallyBlock.isPresent()) {
      return new TryStatementImpl(
        tryToken,
        blockTree,
        optionalList(catchBlocks),
        finallyBlock.get().first(),
        finallyBlock.get().second()
      );
    } else {
      return new TryStatementImpl(
        tryToken,
        blockTree,
        optionalList(catchBlocks)
      );
    }
  }

  public NamespaceNameTree namespaceName(
    Optional<InternalSyntaxToken> separator,
    Optional<List<Tuple<InternalSyntaxToken, InternalSyntaxToken>>> listOptional,
    InternalSyntaxToken name
  ) {
    return namespaceName(separator.orNull(), null, null, listOptional, name);
  }


  public NamespaceNameTree namespaceName(
    InternalSyntaxToken namespaceToken,
    InternalSyntaxToken separator,
    Optional<List<Tuple<InternalSyntaxToken, InternalSyntaxToken>>> listOptional,
    InternalSyntaxToken name
  ) {
    return namespaceName(null, namespaceToken, separator, listOptional, name);
  }

  private NamespaceNameTree namespaceName(
    @Nullable InternalSyntaxToken absoluteSeparator,
    @Nullable InternalSyntaxToken namespaceToken,
    @Nullable InternalSyntaxToken separator,
    Optional<List<Tuple<InternalSyntaxToken, InternalSyntaxToken>>> listOptional,
    InternalSyntaxToken name
  ) {

    ImmutableList.Builder<IdentifierTree> elements = ImmutableList.builder();
    ImmutableList.Builder<InternalSyntaxToken> separators = ImmutableList.builder();

    if (namespaceToken != null && separator != null) {
      elements.add(new IdentifierTreeImpl(namespaceToken));
      separators.add(separator);
    }

    if (listOptional.isPresent()) {
      for (Tuple<InternalSyntaxToken, InternalSyntaxToken> tuple : listOptional.get()) {
        elements.add(new IdentifierTreeImpl(tuple.first()));
        separators.add(tuple.second());
      }
    }

    return new NamespaceNameTreeImpl(absoluteSeparator, new SeparatedList<>(elements.build(), separators.build()), new IdentifierTreeImpl(name));

  }

  public CatchBlockTree catchBlock(
    InternalSyntaxToken catchToken, InternalSyntaxToken lParenthesis,
    NamespaceNameTree exceptionType, InternalSyntaxToken variable,
    InternalSyntaxToken rParenthsis, BlockTree block
  ) {
    return new CatchBlockTreeImpl(
      catchToken,
      lParenthesis,
      exceptionType,
      new VariableIdentifierTreeImpl(new IdentifierTreeImpl(variable)),
      rParenthsis,
      block
    );
  }

  public EmptyStatementTree emptyStatement(InternalSyntaxToken semicolonToken) {
    return new EmptyStatementImpl(semicolonToken);
  }

  public ThrowStatementTree throwStatement(InternalSyntaxToken throwToken, ExpressionTree expression, InternalSyntaxToken eosToken) {
    return new ThrowStatementTreeImpl(throwToken, expression, eosToken);
  }

  public ForEachStatementTree forEachStatement(ForEachStatementHeader header, StatementTree statement) {
    return new ForEachStatementTreeImpl(header, statement);
  }

  public ForEachStatementTree forEachStatementAlternative(
    ForEachStatementHeader header,
    InternalSyntaxToken colonToken, Optional<List<StatementTree>> statements, InternalSyntaxToken endForEachToken, InternalSyntaxToken eosToken
  ) {
    return new ForEachStatementTreeImpl(header, colonToken, optionalList(statements), endForEachToken, eosToken);
  }

  public ForEachStatementHeader forEachStatementHeader(
    InternalSyntaxToken forEachToken, InternalSyntaxToken openParenthesisToken,
    ExpressionTree expression, InternalSyntaxToken asToken, Optional<Tuple<ExpressionTree, InternalSyntaxToken>> optionalKey, ExpressionTree value,
    InternalSyntaxToken closeParenthesisToken
  ) {
    return new ForEachStatementHeader(
      forEachToken, openParenthesisToken,
      expression, asToken, getForEachKey(optionalKey), getForEachArrow(optionalKey), value,
      closeParenthesisToken
    );
  }

  @Nullable
  private ExpressionTree getForEachKey(Optional<Tuple<ExpressionTree, InternalSyntaxToken>> optionalKey) {
    if (optionalKey.isPresent()) {
      return optionalKey.get().first();
    } else {
      return null;
    }
  }

  @Nullable
  private InternalSyntaxToken getForEachArrow(Optional<Tuple<ExpressionTree, InternalSyntaxToken>> optionalKey) {
    if (optionalKey.isPresent()) {
      return optionalKey.get().second();
    } else {
      return null;
    }
  }

  public ForStatementHeader forStatementHeader(
    InternalSyntaxToken forToken, InternalSyntaxToken lParenthesis,
    Optional<SeparatedList<ExpressionTree>> init, InternalSyntaxToken semicolon1,
    Optional<SeparatedList<ExpressionTree>> condition, InternalSyntaxToken semicolon2,
    Optional<SeparatedList<ExpressionTree>> update, InternalSyntaxToken rParenthesis
  ) {
    return new ForStatementHeader(
      forToken, lParenthesis,
      optionalSeparatedList(init),
      semicolon1,
      optionalSeparatedList(condition),
      semicolon2,
      optionalSeparatedList(update),
      rParenthesis
    );
  }

  public ForStatementTree forStatement(ForStatementHeader forStatementHeader, StatementTree statement) {
    return new ForStatementTreeImpl(forStatementHeader, statement);
  }

  public ForStatementTree forStatementAlternative(
    ForStatementHeader forStatementHeader, InternalSyntaxToken colonToken,
    Optional<List<StatementTree>> statements, InternalSyntaxToken endForToken, InternalSyntaxToken eos
  ) {
    return new ForStatementTreeImpl(forStatementHeader, colonToken, optionalList(statements), endForToken, eos);
  }

  public SeparatedList<ExpressionTree> forExpr(ExpressionTree expression, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> listOptional) {
    return separatedList(expression, listOptional);
  }

  public ElseClauseTree elseClause(InternalSyntaxToken elseToken, StatementTree statement) {
    return new ElseClauseTreeImpl(elseToken, statement);
  }

  public IfStatementTree ifStatement(
    InternalSyntaxToken ifToken, ParenthesisedExpressionTree expression, StatementTree statement,
    Optional<List<ElseifClauseTree>> elseIfClauses, Optional<ElseClauseTree> elseClause
  ) {
    return new IfStatementTreeImpl(ifToken, expression, statement, optionalList(elseIfClauses), elseClause.orNull());
  }

  public ElseifClauseTree elseifClause(InternalSyntaxToken elseifToken, ParenthesisedExpressionTree condition, StatementTree statement) {
    return new ElseifClauseTreeImpl(elseifToken, condition, statement);
  }

  public IfStatementTree alternativeIfStatement(
    InternalSyntaxToken ifToken, ParenthesisedExpressionTree condition, InternalSyntaxToken colonToken,
    Optional<List<StatementTree>> statements, Optional<List<ElseifClauseTree>> elseifClauses, Optional<ElseClauseTree> elseClause,
    InternalSyntaxToken endIfToken, InternalSyntaxToken eosToken
  ) {
    return new IfStatementTreeImpl(
      ifToken,
      condition,
      colonToken,
      optionalList(statements),
      optionalList(elseifClauses),
      elseClause.orNull(),
      endIfToken,
      eosToken
    );
  }

  public ElseClauseTree alternativeElseClause(InternalSyntaxToken elseToken, InternalSyntaxToken colonToken, Optional<List<StatementTree>> statements) {
    return new ElseClauseTreeImpl(
      elseToken,
      colonToken,
      optionalList(statements)
    );
  }

  public ElseifClauseTree alternativeElseifClause(
    InternalSyntaxToken elseifToken, ParenthesisedExpressionTree condition, InternalSyntaxToken colonToken,
    Optional<List<StatementTree>> statements
  ) {
    return new ElseifClauseTreeImpl(
      elseifToken,
      condition,
      colonToken,
      optionalList(statements)
    );
  }

  public DoWhileStatementTree doWhileStatement(
    InternalSyntaxToken doToken, StatementTree statement,
    InternalSyntaxToken whileToken, ParenthesisedExpressionTree condition,
    InternalSyntaxToken eosToken
  ) {
    return new DoWhileStatementTreeImpl(
      doToken,
      statement,
      whileToken,
      condition,
      eosToken
    );
  }

  public WhileStatementTree whileStatement(InternalSyntaxToken whileToken, ParenthesisedExpressionTree condition, StatementTree statement) {
    return new WhileStatementTreeImpl(whileToken, condition, statement);
  }

  public WhileStatementTree alternativeWhileStatement(
    InternalSyntaxToken whileToken, ParenthesisedExpressionTree condition, InternalSyntaxToken colonToken,
    Optional<List<StatementTree>> statements, InternalSyntaxToken endwhileToken, InternalSyntaxToken eosToken) {
    return new WhileStatementTreeImpl(
      whileToken,
      condition,
      colonToken,
      optionalList(statements),
      endwhileToken,
      eosToken
    );
  }

  public SwitchStatementTree switchStatement(
    InternalSyntaxToken switchToken, ParenthesisedExpressionTree expression, InternalSyntaxToken openCurlyBraceToken,
    Optional<InternalSyntaxToken> semicolonToken,
    Optional<List<SwitchCaseClauseTree>> switchCaseClauses,
    InternalSyntaxToken closeCurlyBraceToken
  ) {
    return new SwitchStatementTreeImpl(
      switchToken,
      expression,
      openCurlyBraceToken,
      semicolonToken.orNull(),
      optionalList(switchCaseClauses),
      closeCurlyBraceToken
    );
  }

  public SwitchStatementTree alternativeSwitchStatement(
    InternalSyntaxToken switchToken, ParenthesisedExpressionTree expression, InternalSyntaxToken colonToken,
    Optional<InternalSyntaxToken> semicolonToken,
    Optional<List<SwitchCaseClauseTree>> switchCaseClauses,
    InternalSyntaxToken endswitchToken, InternalSyntaxToken eosToken
  ) {
    return new SwitchStatementTreeImpl(
      switchToken,
      expression,
      colonToken,
      semicolonToken.orNull(),
      optionalList(switchCaseClauses),
      endswitchToken,
      eosToken
    );
  }

  public CaseClauseTree caseClause(InternalSyntaxToken caseToken, ExpressionTree expression, InternalSyntaxToken caseSeparatorToken, Optional<List<StatementTree>> statements) {
    return new CaseClauseTreeImpl(
      caseToken,
      expression,
      caseSeparatorToken,
      optionalList(statements)
    );
  }

  public DefaultClauseTree defaultClause(InternalSyntaxToken defaultToken, InternalSyntaxToken caseSeparatorToken, Optional<List<StatementTree>> statements) {
    return new DefaultClauseTreeImpl(
      defaultToken,
      caseSeparatorToken,
      optionalList(statements)
    );
  }

  public YieldStatementTree yieldStatement(YieldExpressionTree yieldExpression, InternalSyntaxToken eosToken) {
    return new YieldStatementTreeImpl(yieldExpression, eosToken);
  }

  public UnsetVariableStatementTree unsetVariableStatement(
    InternalSyntaxToken unsetToken, InternalSyntaxToken openParenthesisToken,
    ExpressionTree expression, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> list,
    InternalSyntaxToken closeParenthesisToken, InternalSyntaxToken eosToken
  ) {
    return new UnsetVariableStatementTreeImpl(
      unsetToken,
      openParenthesisToken,
      separatedList(expression, list),
      closeParenthesisToken,
      eosToken
    );
  }

  public NamespaceStatementTree namespaceStatement(InternalSyntaxToken namespaceToken, NamespaceNameTree namespaceName, InternalSyntaxToken eosToken) {
    return new NamespaceStatementTreeImpl(
      namespaceToken,
      namespaceName,
      eosToken
    );
  }

  public NamespaceStatementTree blockNamespaceStatement(
    InternalSyntaxToken namespaceToken, Optional<NamespaceNameTree> namespaceName,
    InternalSyntaxToken openCurlyBrace, Optional<List<StatementTree>> statements, InternalSyntaxToken closeCurlyBrace
  ) {
    return new NamespaceStatementTreeImpl(
      namespaceToken,
      namespaceName.orNull(),
      openCurlyBrace,
      optionalList(statements),
      closeCurlyBrace
    );
  }

  public InlineHTMLTree inlineHTML(InternalSyntaxToken inlineHTMLToken) {
    return new InlineHTMLTreeImpl(inlineHTMLToken);
  }

  public DeclareStatementTree shortDeclareStatement(DeclareStatementHead declareStatementHead, InternalSyntaxToken eosToken) {
    return new DeclareStatementTreeImpl(declareStatementHead, eosToken);
  }

  public DeclareStatementHead declareStatementHead(
    InternalSyntaxToken declareToken, InternalSyntaxToken openParenthesisToken,
    VariableDeclarationTree firstDirective, Optional<List<Tuple<InternalSyntaxToken, VariableDeclarationTree>>> optionalDirectives,
    InternalSyntaxToken closeParenthesisToken
  ) {
    return new DeclareStatementHead(
      declareToken,
      openParenthesisToken,
      separatedList(firstDirective, optionalDirectives),
      closeParenthesisToken
    );
  }

  public DeclareStatementTree declareStatementWithOneStatement(DeclareStatementHead declareStatementHead, StatementTree statement) {
    return new DeclareStatementTreeImpl(declareStatementHead, statement);
  }

  public DeclareStatementTree alternativeDeclareStatement(
    DeclareStatementHead declareStatementHead, InternalSyntaxToken colonToken,
    Optional<List<StatementTree>> statements,
    InternalSyntaxToken enddeclareToken, InternalSyntaxToken eosToken
  ) {
    return new DeclareStatementTreeImpl(declareStatementHead, colonToken, optionalList(statements), enddeclareToken, eosToken);
  }

  public StaticStatementTree staticStatement(
    InternalSyntaxToken staticToken, VariableDeclarationTree variable,
    Optional<List<Tuple<InternalSyntaxToken, VariableDeclarationTree>>> listOptional,
    InternalSyntaxToken eosToken
  ) {
    return new StaticStatementTreeImpl(staticToken, separatedList(variable, listOptional), eosToken);
  }

  /**
   * [ END ] Statement
   */

  /**
   * [ START ] Expression
   */

  public ExpressionTree castExpression(InternalSyntaxToken leftParenthesis, InternalSyntaxToken type, InternalSyntaxToken rightParenthesis, ExpressionTree expression) {
    return new CastExpressionTreeImpl(leftParenthesis, type, rightParenthesis, expression);
  }

  public ExpressionTree prefixExpr(InternalSyntaxToken operator, ExpressionTree expression) {
    Kind kind = UNARY_EXPRESSION_KINDS_BY_OPERATOR.get(operator.text());
    if (kind == null) {
      throw new IllegalArgumentException("Mapping not found for unary operator " + operator.text());
    }
    return new PrefixExpressionTreeImpl(kind, operator, expression);
  }

  public ExpressionTree concatenationExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree multiplicativeExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree additiveExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree shiftExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree relationalExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree equalityExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree bitwiseAndExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree bitwiseXorExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree bitwiseOrExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree logicalAndExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree logicalXorExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree logicalOrExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return binaryExpression(exp1, operatorsAndOperands);
  }

  private ExpressionTree binaryExpression(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    if (!operatorsAndOperands.isPresent()) {
      return exp1;
    }

    ExpressionTree result = exp1;
    for (Tuple<InternalSyntaxToken, ExpressionTree> t : operatorsAndOperands.get()) {
      result = new BinaryExpressionTreeImpl(binaryKind(t.first()), result, t.first(), t.second());
    }
    return result;
  }

  private static Kind binaryKind(InternalSyntaxToken token) {
    Kind kind = BINARY_EXPRESSION_KINDS_BY_OPERATOR.get(token.text());
    if (kind == null) {
      throw new IllegalArgumentException("Mapping not found for binary operator " + token.text());
    }
    return kind;
  }

  public LiteralTree numericLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.NUMERIC_LITERAL, token);
  }

  public LiteralTree regularStringLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.REGULAR_STRING_LITERAL, token);
  }

  public LiteralTree booleanLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.BOOLEAN_LITERAL, token);
  }

  public LiteralTree nullLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.NULL_LITERAL, token);
  }

  public LiteralTree magicConstantLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.MAGIC_CONSTANT, token);
  }

  public LiteralTree heredocLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.HEREDOC_LITERAL, token);
  }

  public ExpandableStringCharactersTree expandableStringCharacters(InternalSyntaxToken token) {
    return new ExpandableStringCharactersTreeImpl(token);
  }

  public VariableIdentifierTree expandableStringVariableIdentifier(InternalSyntaxToken token) {
    return new VariableIdentifierTreeImpl(new IdentifierTreeImpl(token));
  }

  public IdentifierTree identifier(InternalSyntaxToken token) {
    return new IdentifierTreeImpl(token);
  }

  public ArrayAccessTree expandableArrayAccess(InternalSyntaxToken openBracket, ExpressionTree offset, InternalSyntaxToken closeBracket) {
    return new ArrayAccessTreeImpl(openBracket, offset, closeBracket);
  }

  public MemberAccessTree expandableObjectMemberAccess(InternalSyntaxToken arrow, IdentifierTree property) {
    return new MemberAccessTreeImpl(Kind.OBJECT_MEMBER_ACCESS, arrow, property);
  }

  public ExpressionTree encapsulatedSimpleVar(VariableIdentifierTree variableIdentifier, Optional<ExpressionTree> partial) {
    if (partial.isPresent()) {

      if (partial.get() instanceof ArrayAccessTree) {
        ((ArrayAccessTreeImpl) partial.get()).complete(variableIdentifier);
      } else {
        ((MemberAccessTreeImpl) partial.get()).complete(variableIdentifier);
      }
      return partial.get();
    }

    return variableIdentifier;
  }

  public ExpressionTree expressionRecovery(InternalSyntaxToken token) {
    return new IdentifierTreeImpl(token);
  }

  public ExpressionTree encapsulatedSemiComplexVariable(InternalSyntaxToken openDollarCurly, ExpressionTree expressionTree, InternalSyntaxToken closeCurly) {
    return new CompoundVariableTreeImpl(openDollarCurly, expressionTree, closeCurly);
  }

  public VariableIdentifierTree encapsulatedVariableIdentifier(InternalSyntaxToken spaces, InternalSyntaxToken variableIdentifier) {
    return new VariableIdentifierTreeImpl(new IdentifierTreeImpl(variableIdentifier));
  }

  public ExpressionTree encapsulatedComplexVariable(InternalSyntaxToken openCurly, Tree lookahead, ExpressionTree expression, InternalSyntaxToken closeCurly) {
    return new ComputedVariableTreeImpl(openCurly, expression, closeCurly);
  }

  public ExpandableStringLiteralTree expandableStringLiteral(
    Tree spacing, InternalSyntaxToken openDoubleQuote,
    List<ExpressionTree> expressions,
    InternalSyntaxToken closeDoubleQuote
  ) {
    return new ExpandableStringLiteralTreeImpl(openDoubleQuote, expressions, closeDoubleQuote);
  }

  public YieldExpressionTree yieldExpression(InternalSyntaxToken yieldToken, ExpressionTree expr1, Optional<Tuple<InternalSyntaxToken, ExpressionTree>> expr2) {
    if (expr2.isPresent()) {
      return new YieldExpressionTreeImpl(yieldToken, expr1, expr2.get().first(), expr2.get().second());
    }
    return new YieldExpressionTreeImpl(yieldToken, expr1);
  }

  public ParenthesisedExpressionTree parenthesizedExpression(InternalSyntaxToken openParenthesis, ExpressionTree expression, InternalSyntaxToken closeParenthesis) {
    return new ParenthesizedExpressionTreeImpl(openParenthesis, expression, closeParenthesis);
  }

  public ListExpressionTree listExpression(
    InternalSyntaxToken listToken, InternalSyntaxToken openParenthesis,
    Optional<Tuple<ExpressionTree, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>>>> elements,
    InternalSyntaxToken closeParenthesis
  ) {
    SeparatedList<ExpressionTree> list;

    if (elements.isPresent()) {
      list = separatedList(elements.get().first(), elements.get().second());
    } else {
      list = SeparatedList.empty();
    }

    return new ListExpressionTreeImpl(listToken, openParenthesis, list, closeParenthesis);
  }

  public AssignmentExpressionTree listExpressionAssignment(ExpressionTree listExpression, InternalSyntaxToken equalToken, ExpressionTree expression) {
    return new AssignmentExpressionTreeImpl(Kind.ASSIGNMENT, listExpression, equalToken, expression);
  }

  public ComputedVariableTree computedVariableName(InternalSyntaxToken openCurly, ExpressionTree expression, InternalSyntaxToken closeCurly) {
    return new ComputedVariableTreeImpl(openCurly, expression, closeCurly);
  }

  public VariableIdentifierTree variableIdentifier(InternalSyntaxToken variableIdentifier) {
    return new VariableIdentifierTreeImpl(new IdentifierTreeImpl(variableIdentifier));
  }

  public CompoundVariableTree compoundVariable(InternalSyntaxToken openDollarCurly, ExpressionTree expression, InternalSyntaxToken closeDollarCurly) {
    return new CompoundVariableTreeImpl(openDollarCurly, expression, closeDollarCurly);
  }

  public ArrayAccessTree dimensionalOffset(InternalSyntaxToken openCurly, Optional<ExpressionTree> expression, InternalSyntaxToken closeCurly) {
    if (expression.isPresent()) {
      return new ArrayAccessTreeImpl(openCurly, expression.get(), closeCurly);
    }
    return new ArrayAccessTreeImpl(openCurly, closeCurly);
  }

  public ExpressionTree variableWithoutObjects(Optional<List<InternalSyntaxToken>> dollars, VariableTree compoundVariable, Optional<List<ArrayAccessTree>> offsets) {
    ExpressionTree result = compoundVariable;
    for (ExpressionTree partialArrayAccess : optionalList(offsets)) {
      result = ((ArrayAccessTreeImpl) partialArrayAccess).complete(result);
    }

    if (dollars.isPresent()) {
      result = new VariableVariableTreeImpl(dollars.get(), result);
    }

    return result;
  }

  public ArrayAccessTree alternativeDimensionalOffset(InternalSyntaxToken openBrace, Optional<ExpressionTree> offset, InternalSyntaxToken closeBrace) {
    if (offset.isPresent()) {
      return new ArrayAccessTreeImpl(openBrace, offset.get(), closeBrace);
    }
    return new ArrayAccessTreeImpl(openBrace, closeBrace);
  }

  public IdentifierTree newStaticIdentifier(InternalSyntaxToken staticToken) {
    return new IdentifierTreeImpl(staticToken);
  }

  public ReferenceVariableTree referenceVariable(InternalSyntaxToken ampersand, ExpressionTree variable) {
    return new ReferenceVariableTreeImpl(ampersand, variable);
  }

  public SpreadArgumentTree spreadArgument(InternalSyntaxToken ellipsis, ExpressionTree expression) {
    return new SpreadArgumentTreeImpl(ellipsis, expression);
  }

  public FunctionCallTree functionCallParameterList(
    InternalSyntaxToken openParenthesis,
    Optional<Tuple<ExpressionTree, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>>>> arguments,
    InternalSyntaxToken closeParenthesis
  ) {
    SeparatedList<ExpressionTree> list;
    if (arguments.isPresent()) {
      list = separatedList(arguments.get().first(), arguments.get().second());
    } else {
      list = SeparatedList.empty();
    }

    return new FunctionCallTreeImpl(openParenthesis, list, closeParenthesis);
  }

  public MemberAccessTree classMemberAccess(InternalSyntaxToken token, Tree member) {
    return new MemberAccessTreeImpl(Kind.CLASS_MEMBER_ACCESS, token, member);
  }

  public ExpressionTree objectDimensionalList(ExpressionTree variableName, Optional<List<ArrayAccessTree>> dimensionalOffsets) {
    ExpressionTree result = variableName;

    for (ArrayAccessTree arrayAccess : optionalList(dimensionalOffsets)) {
      result = ((ArrayAccessTreeImpl) arrayAccess).complete(result);
    }

    return result;
  }

  public IdentifierTree variableName(InternalSyntaxToken token) {
    return new IdentifierTreeImpl(token);
  }

  public MemberAccessTree objectMemberAccess(InternalSyntaxToken accessToken, ExpressionTree member) {
    return new MemberAccessTreeImpl(Kind.OBJECT_MEMBER_ACCESS, accessToken, member);
  }

  public ExpressionTree memberExpression(ExpressionTree object, Optional<List<ExpressionTree>> memberAccesses) {
    ExpressionTree result = object;

    for (ExpressionTree memberAccess : optionalList(memberAccesses)) {
      if (memberAccess.is(Kind.OBJECT_MEMBER_ACCESS, Kind.CLASS_MEMBER_ACCESS)) {
        result = ((MemberAccessTreeImpl) memberAccess).complete(result);

      } else if (memberAccess.is(Kind.ARRAY_ACCESS)) {
        result = ((ArrayAccessTreeImpl) memberAccess).complete(result);

      } else if (memberAccess.is(Kind.FUNCTION_CALL)) {
        result = ((FunctionCallTreeImpl) memberAccess).complete(result);
      }
    }

    return result;
  }

  public VariableTree lexicalVariable(Optional<InternalSyntaxToken> ampersandToken, VariableIdentifierTree variableIdentifier) {
    return ampersandToken.isPresent()
      ? new ReferenceVariableTreeImpl(ampersandToken.get(), variableIdentifier)
      : variableIdentifier;
  }

  public LexicalVariablesTree lexicalVariables(
    InternalSyntaxToken useToken, InternalSyntaxToken openParenthesis,
    VariableTree variable, Optional<List<Tuple<InternalSyntaxToken, VariableTree>>> variableRest,
    InternalSyntaxToken closeParenthesis
  ) {
    return new LexicalVariablesTreeImpl(useToken, openParenthesis, separatedList(variable, variableRest), closeParenthesis);
  }

  public FunctionCallTree internalFunction1(
    InternalSyntaxToken issetToken, InternalSyntaxToken openParenthesis,
    ExpressionTree expression, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> expressionRest,
    InternalSyntaxToken closeParenthesis
  ) {
    return new FunctionCallTreeImpl(
      new IdentifierTreeImpl(issetToken),
      openParenthesis,
      separatedList(expression, expressionRest),
      closeParenthesis);
  }

  public FunctionCallTree internalFunction2(
    InternalSyntaxToken functionNameToken, InternalSyntaxToken openParenthesis,
    ExpressionTree expression,
    InternalSyntaxToken closeParenthesis
  ) {
    return new FunctionCallTreeImpl(
      new IdentifierTreeImpl(functionNameToken),
      openParenthesis,
      new SeparatedList(ImmutableList.of(expression), ImmutableList.<InternalSyntaxToken>of()),
      closeParenthesis);
  }

  public FunctionCallTree internalFunction3(InternalSyntaxToken includeOnceToken, ExpressionTree expression) {
    return new FunctionCallTreeImpl(
      new IdentifierTreeImpl(includeOnceToken),
      new SeparatedList(ImmutableList.of(expression), ImmutableList.<InternalSyntaxToken>of()));
  }

  public ArrayPairTree arrayPair1(ExpressionTree expression, Optional<Tuple<InternalSyntaxToken, ExpressionTree>> pairExpression) {
    if (pairExpression.isPresent()) {
      return new ArrayPairTreeImpl(expression, pairExpression.get().first(), pairExpression.get().second());
    }
    return new ArrayPairTreeImpl(expression);
  }

  public ArrayPairTree arrayPair2(ReferenceVariableTree referenceVariableTree) {
    return new ArrayPairTreeImpl(referenceVariableTree);
  }

  public SeparatedList<ArrayPairTree> arrayInitializerList(
    ArrayPairTree firstElement,
    Optional<List<Tuple<InternalSyntaxToken, ArrayPairTree>>> restElements,
    Optional<InternalSyntaxToken> trailingComma
  ) {
    return separatedList(firstElement, restElements, trailingComma.orNull());
  }

  public ArrayInitializerTree newArrayInitFunction(
    InternalSyntaxToken arrayToken, InternalSyntaxToken openParenthesis,
    Optional<SeparatedList<ArrayPairTree>> elements,
    InternalSyntaxToken closeParenthesis
  ) {
    return new ArrayInitializerFunctionTreeImpl(
      arrayToken,
      openParenthesis,
      elements.isPresent() ? elements.get() : new SeparatedList<>(ImmutableList.<ArrayPairTree>of(), ImmutableList.<InternalSyntaxToken>of()),
      closeParenthesis);
  }

  public ArrayInitializerTree newArrayInitBracket(InternalSyntaxToken openBracket, Optional<SeparatedList<ArrayPairTree>> elements, InternalSyntaxToken closeBracket) {
    return new ArrayInitializerBracketTreeImpl(
      openBracket,
      elements.isPresent() ? elements.get() : new SeparatedList<>(ImmutableList.<ArrayPairTree>of(), ImmutableList.<InternalSyntaxToken>of()),
      closeBracket);
  }

  public FunctionExpressionTree functionExpression(Optional<InternalSyntaxToken> staticToken, InternalSyntaxToken functionToken, Optional<InternalSyntaxToken> ampersandToken,
    ParameterListTree parameters, Optional<LexicalVariablesTree> lexicalVariables, BlockTree block) {

    return new FunctionExpressionTreeImpl(
      staticToken.orNull(),
      functionToken,
      ampersandToken.orNull(),
      parameters,
      lexicalVariables.orNull(),
      block);
  }

  public NewExpressionTree newExpression(InternalSyntaxToken newToken, ExpressionTree expression) {
    return new NewExpressionTreeImpl(newToken, expression);
  }

  public ExitTreeImpl newExitExpression(InternalSyntaxToken openParenthesis, Optional<ExpressionTree> expressionTreeOptional, InternalSyntaxToken closeParenthesis) {
    return new ExitTreeImpl(openParenthesis, expressionTreeOptional.orNull(), closeParenthesis);
  }

  public ExitTree completeExitExpression(InternalSyntaxToken exitOrDie, Optional<ExitTreeImpl> partial) {
    return partial.isPresent() ? partial.get().complete(exitOrDie) : new ExitTreeImpl(exitOrDie);
  }

  public ExpressionTree combinedScalarOffset(ArrayInitializerTree arrayInitialiser, Optional<List<ArrayAccessTree>> offsets) {
    ExpressionTree result = arrayInitialiser;
    for (ArrayAccessTree offset : optionalList(offsets)) {
      result = ((ArrayAccessTreeImpl) offset).complete(result);
    }

    return result;
  }

  public ExpressionTree postfixExpression(ExpressionTree expression, Optional<Object> optional) {
    if (optional.isPresent()) {

      if (optional.get() instanceof SyntaxToken) {
        SyntaxToken operator = ((SyntaxToken) optional.get());

        return new PostfixExpressionTreeImpl(
          operator.text().equals(PHPPunctuator.INC.getValue()) ? Kind.POSTFIX_INCREMENT : Kind.POSTFIX_DECREMENT,
          expression,
          operator);

      } else {
        Tuple<InternalSyntaxToken, ExpressionTree> tuple = ((Tuple) optional.get());
        return new BinaryExpressionTreeImpl(Kind.INSTANCE_OF, expression, tuple.first(), tuple.second);
      }
    }

    return expression;
  }

  public AssignmentExpressionTree assignmentExpression(ExpressionTree lhs, InternalSyntaxToken operatorToken, ExpressionTree rhs) {
    String operator = operatorToken.text();
    Kind kind = Kind.ASSIGNMENT;

    if ("*=".equals(operator)) {
      kind = Kind.MULTIPLY_ASSIGNMENT;
    } else if ("/=".equals(operator)) {
      kind = Kind.DIVIDE_ASSIGNMENT;
    } else if ("%=".equals(operator)) {
      kind = Kind.REMAINDER_ASSIGNMENT;
    } else if ("+=".equals(operator)) {
      kind = Kind.PLUS_ASSIGNMENT;
    } else if ("-=".equals(operator)) {
      kind = Kind.MINUS_ASSIGNMENT;
    } else if ("<<=".equals(operator)) {
      kind = Kind.LEFT_SHIFT_ASSIGNMENT;
    } else if (">>=".equals(operator)) {
      kind = Kind.RIGHT_SHIFT_ASSIGNMENT;
    } else if ("&=".equals(operator)) {
      kind = Kind.AND_ASSIGNMENT;
    } else if ("^=".equals(operator)) {
      kind = Kind.XOR_ASSIGNMENT;
    } else if ("|=".equals(operator)) {
      kind = Kind.OR_ASSIGNMENT;
    } else if (".=".equals(operator)) {
      kind = Kind.CONCATENATION_ASSIGNMENT;
    }

    return new AssignmentExpressionTreeImpl(kind, lhs, operatorToken, rhs);
  }

  public AssignmentByReferenceTree assignmentByReference(ExpressionTree lhs, InternalSyntaxToken equToken, InternalSyntaxToken ampersandToken, ExpressionTree rhs) {
    return new AssignmentByReferenceTreeImpl(lhs, equToken, ampersandToken, rhs);
  }

  public ConditionalExpressionTreeImpl newConditionalExpr(InternalSyntaxToken queryToken, Optional<ExpressionTree> trueExpression, InternalSyntaxToken colonToken, ExpressionTree falseExpression) {
    return new ConditionalExpressionTreeImpl(queryToken, trueExpression.orNull(), colonToken, falseExpression);
  }

  public ExpressionTree completeConditionalExpr(ExpressionTree expression, Optional<ConditionalExpressionTreeImpl> partial) {
    return partial.isPresent() ? partial.get().complete(expression) : expression;
  }

  public ExpressionTree expression(InternalSyntaxToken token) {
    return new VariableIdentifierTreeImpl(new IdentifierTreeImpl(token));
  }

  /**
   * [ END ] Expression
   */

  public static class Tuple<T, U> {

    private final T first;
    private final U second;

    public Tuple(T first, U second) {
      super();

      this.first = first;
      this.second = second;
    }

    public T first() {
      return first;
    }

    public U second() {
      return second;
    }
  }

  private <T, U> Tuple<T, U> newTuple(T first, U second) {
    return new Tuple<T, U>(first, second);
  }

  public <T, U> Tuple<T, U> newTuple1(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple2(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple3(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple4(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple5(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple6(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple7(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple8(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple9(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple10(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple11(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple12(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple13(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple14(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple15(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple16(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple17(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple18(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple19(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple20(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple21(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple22(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple23(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple24(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple25(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple26(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple27(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple28(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple29(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple30(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple50(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple51(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple52(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple53(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple54(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple55(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple60(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple61(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple62(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple63(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple64(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple65(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple66(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple67(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple68(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple69(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple70(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple71(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple90(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple91(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple92(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple93(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple94(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple95(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple96(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple97(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple98(T first, U second) {
    return newTuple(first, second);
  }

  public List<SyntaxToken> singleToken(SyntaxToken token) {
    return ImmutableList.of(token);
  }
}
