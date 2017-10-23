/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.tree.impl.expression;

import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.php.parser.TreeFactory.Tuple;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ArrayAssignmentPatternTreeImpl extends PHPTree implements ArrayAssignmentPatternTree {

  private static final Kind KIND = Kind.ARRAY_ASSIGNMENT_PATTERN;
  private final SyntaxToken openBracket;
  private final List<Tree> elementsAndSeparators = new ArrayList<>();
  private final List<Optional<Tree>> elements = new ArrayList<>();
  private final SyntaxToken closeBracket;

  public ArrayAssignmentPatternTreeImpl(SyntaxToken openBracket, @Nullable Tree firstElement, List<Tuple<SyntaxToken,Optional<Tree>>> rest, SyntaxToken closeBracket) {
    this.openBracket = openBracket;
    this.closeBracket = closeBracket;

    elements.add(Optional.ofNullable(firstElement));
    if (firstElement != null) {
      elementsAndSeparators.add(firstElement);
    }
    for (Tuple<SyntaxToken, Optional<Tree>> tuple : rest) {
      elementsAndSeparators.add(tuple.first());
      Optional<Tree> second = tuple.second();
      elements.add(second);
      if (second.isPresent()) {
        elementsAndSeparators.add(second.get());
      }
    }
  }

  @Override
  public SyntaxToken openBracketToken() {
    return openBracket;
  }

  @Override
  public List<Optional<Tree>> elements() {
    return elements;
  }

  @Override
  public SyntaxToken closeBracketToken() {
    return closeBracket;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.singletonIterator(openBracket),
      elementsAndSeparators.iterator(),
      Iterators.singletonIterator(closeBracket));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitArrayAssignmentPattern(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

}