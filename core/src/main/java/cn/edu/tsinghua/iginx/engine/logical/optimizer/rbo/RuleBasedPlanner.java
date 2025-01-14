package cn.edu.tsinghua.iginx.engine.logical.optimizer.rbo;

import cn.edu.tsinghua.iginx.engine.logical.optimizer.core.Operand;
import cn.edu.tsinghua.iginx.engine.logical.optimizer.core.Planner;
import cn.edu.tsinghua.iginx.engine.logical.optimizer.core.RuleCall;
import cn.edu.tsinghua.iginx.engine.logical.optimizer.core.iterator.DeepFirstIterator;
import cn.edu.tsinghua.iginx.engine.logical.optimizer.core.iterator.LeveledIterator;
import cn.edu.tsinghua.iginx.engine.logical.optimizer.core.iterator.MatchOrder;
import cn.edu.tsinghua.iginx.engine.logical.optimizer.core.iterator.ReverseDeepFirstIterator;
import cn.edu.tsinghua.iginx.engine.logical.optimizer.core.iterator.ReverseLeveledIterator;
import cn.edu.tsinghua.iginx.engine.logical.optimizer.core.iterator.TreeIterator;
import cn.edu.tsinghua.iginx.engine.logical.optimizer.rules.Rule;
import cn.edu.tsinghua.iginx.engine.logical.optimizer.rules.RuleCollection;
import cn.edu.tsinghua.iginx.engine.shared.operator.Operator;
import cn.edu.tsinghua.iginx.engine.shared.operator.visitor.IndexVisitor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RuleBasedPlanner implements Planner {

  private Operator root;

  private final RuleCollection ruleCollection = RuleCollection.getInstance();

  private Map<Operator, Operator> parentIndex;
  private Map<Operator, List<Operator>> childrenIndex;

  private int matchCount = 0;
  private int matchLimit = Integer.MAX_VALUE;

  private final long startTime = System.currentTimeMillis();
  private long limitTime = Long.MAX_VALUE;

  private MatchOrder matchOrder = MatchOrder.DeepFirst;

  @Override
  public void unbanRule(Rule rule) {
    ruleCollection.unbanRule(rule);
  }

  @Override
  public void unbanRuleCollection(List<Rule> rules) {
    ruleCollection.unbanRules(rules);
  }

  @Override
  public void setRoot(Operator root) {
    this.root = root;
    updateIndex();
  }

  @Override
  public void setMatchLimit(int matchLimit) {
    this.matchLimit = matchLimit;
  }

  @Override
  public void setLimitTime(long limitTime) {
    this.limitTime = limitTime;
  }

  @Override
  public void setMatchOrder(MatchOrder matchOrder) {
    this.matchOrder = matchOrder;
  }

  @Override
  public Operator findBest() {
    boolean hasMatched = false;
    while (!reachLimit()) {
      TreeIterator treeIt = getTreeIterator();
      while (treeIt.hasNext()) {
        Operator op = treeIt.next();
        Iterator<Rule> rulesIt = ruleCollection.iterator();
        while (rulesIt.hasNext()) {
          Rule rule = rulesIt.next();
          Operand expected = rule.getOperand();

          if (!matchOperand(expected, op)) {
            continue;
          }

          RuleCall ruleCall = new RBORuleCall(op, parentIndex, childrenIndex);
          if (!rule.matches(ruleCall)) {
            continue;
          }

          rule.onMatch(ruleCall);
          matchCount++;
          updateIndex();
        }
      }
      if (!hasMatched) {
        break;
      }
    }
    return root;
  }

  private boolean matchOperand(Operand expected, Operator actual) {
    if (expected == Operand.ANY_OPERAND) {
      return true;
    }
    if (!expected.matches(actual)) {
      return false;
    }
    List<Operand> expectedChildren = expected.getChildren();
    List<Operator> actualChildren = childrenIndex.get(actual);

    int expectedChildrenSize = expectedChildren.size();
    int actualChildrenSize = actualChildren == null ? 0 : actualChildren.size();
    if (expectedChildrenSize != actualChildrenSize) {
      return false;
    }

    for (int i = 0; i < expectedChildrenSize; i++) {
      Operand expectedChild = expectedChildren.get(i);
      Operator actualChild = actualChildren.get(i);
      if (!matchOperand(expectedChild, actualChild)) {
        return false;
      }
    }
    return true;
  }

  private void updateIndex() {
    IndexVisitor visitor = new IndexVisitor();
    root.accept(visitor);
    this.parentIndex = visitor.getParentMap();
    this.childrenIndex = visitor.getChildrenMap();
  }

  private boolean reachLimit() {
    long currentTime = System.currentTimeMillis();
    return matchCount >= matchLimit || currentTime - startTime >= limitTime;
  }

  private TreeIterator getTreeIterator() {
    switch (matchOrder) {
      case DeepFirst:
        return new DeepFirstIterator(root);
      case ReverseDeepFirst:
        return new ReverseDeepFirstIterator(root);
      case ReverseLeveled:
        return new ReverseLeveledIterator(root);
      case Leveled:
      default:
        return new LeveledIterator(root);
    }
  }
}
