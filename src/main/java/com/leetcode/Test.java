package com.leetcode;

class Solution {
    public int subtractProductAndSum(int n) {
        if (n == 0) return 0;
        int sum = 0;
        int p = 1;
        int i = 0;
        while (n != 0) {
            i = n % 10;
            sum += i;
            if (p != 0) p *= i;
            n = n / 10;
        }
        return p - sum;
    }

    public int game(int[] guess, int[] answer) {
        int count = 0;
        if (guess[0] == answer[0]) count++;
        if (guess[1] == answer[1]) count++;
        if (guess[2] == answer[2]) count++;

        return count;
    }

    public int numJewelsInStones(String J, String S) {
        if (J == null || S == null || J.equals("") || S.equals("")) return 0;
        String shortStr;
        String longStr;
        if (J.length() < S.length()) {
            shortStr = J;
            longStr = S;
        } else {
            shortStr = S;
            longStr = J;
        }
        int count = 0;

        for (int i = 0; i < longStr.length(); i++) {
            for (int j = 0; j < shortStr.length(); j++) {
                if (longStr.charAt(i) == shortStr.charAt(j)) count++;
            }
        }
        return count;
    }

    public String defangIPaddr(String address) {
        char dot = '.';
        char leftB = '[';
        char rightB = ']';
        int index = address.indexOf(dot);
        int index1 = address.indexOf(dot, index + 1);
        int index2 = address.lastIndexOf(dot);
        int offset = 6;
        char[] result = new char[address.length() + offset];

        for (int i = address.length() - 1; i >= 0; i--) {
            if (i < index) {
                result[i] = address.charAt(i);
            } else if (i == index || i == index1 || i == index2) {
                result[i + offset] = rightB;
                offset--;
                result[i + offset] = dot;
                offset--;
                result[i + offset] = leftB;
            } else {
                result[i + offset] = address.charAt(i);
            }
        }
        return new String(result);
    }

    public void deleteNode(ListNode node) {
        node.val = node.next.val;
        node.next = node.next.next;
    }

    public ListNode removeElements(ListNode head, int val) {
        if (head == null) return null;
        if (head.next == null) {
            if (head.val == val) return null;
        }
        ListNode dumb = new ListNode(0);
        dumb.next = head;
        ListNode iter = dumb;
        while (iter.next != null) {
            ListNode curr = iter.next;
            if (curr.val == val) {
                iter.next = curr.next;
                continue;
            }
            iter = curr;
        }
        return dumb.next;
    }

}


public class Test {
    public static void main(String[] args) {
        Solution s = new Solution();
        ListNode head = new ListNode(1);
        head.next = new ListNode(2);
        head.next.next = new ListNode(3);
        head.next.next.next = new ListNode(2);
//        head.next.next = new ListNode(3);
        System.out.println("result: " + s.removeElements(head, 2));
    }
}

class ListNode {
    int val;
    ListNode next;

    ListNode(int x) {
        val = x;
    }

    @Override
    public String toString() {
        return "ListNode{" +
                "val=" + val +
                ", next=" + next +
                '}';
    }
}