package regretmin;

import java.util.Arrays;

public class Action {
    private final int[] action;

    public Action(int[] action) {
        this.action = action;
    }

    public int[] getArray() {
        return action;
    }

    public int size() {
        return action.length;
    }

    public void swapSoldiers(int battlefield1, int battlefield2) {
        int temp = action[battlefield1];
        action[battlefield1] = action[battlefield2];
        action[battlefield2] = temp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Arrays.equals(action, ((Action) o).action);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(action);
    }
}
