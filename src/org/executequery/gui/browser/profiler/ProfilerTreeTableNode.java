package org.executequery.gui.browser.profiler;

import org.underworldlabs.swing.treetable.CCTNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfilerTreeTableNode extends CCTNode {

    private ProfilerData data;
    private CCTNode[] childrenArray;
    private final List<CCTNode> childrenList;

    public ProfilerTreeTableNode(ProfilerData data) {
        this.data = data;
        childrenList = new ArrayList<>();
        childrenArray = new CCTNode[0];
    }

    public void add(CCTNode node) {
        childrenList.add(node);
        refreshArray();
    }

    public void remove(CCTNode node) {
        childrenList.remove(node);
        refreshArray();
    }

    public void removeAllChildren() {
        childrenList.clear();
        refreshArray();
    }

    public boolean compareAndMerge(ProfilerTreeTableNode compareNode) {

        if (this.childrenList.size() != compareNode.childrenList.size())
            return false;

        for (int i = 0; i < this.childrenList.size(); i++) {

            ProfilerData data_1 = ((ProfilerTreeTableNode) this.childrenList.get(i)).getData();
            ProfilerData data_2 = ((ProfilerTreeTableNode) compareNode.childrenList.get(i)).getData();
            if (data_1.getCallerId() != data_2.getCallerId() || !Objects.equals(data_1.getProcessName(), data_2.getProcessName()))
                return false;
        }

        return this.getData().compareAndMergeData(compareNode.getData());
    }

    public void setData(ProfilerData data) {
        this.data = data;
    }

    public ProfilerData getData() {
        return data;
    }

    public Object getId() {
        return data.getId();
    }

    public Object getProcessName() {
        return data.getProcessName();
    }

    public Object getProcessType() {
        return data.getProcessType();
    }

    public Object getTotalTime() {
        return data.getTotalTime();
    }

    public Object getTotalTimePercentage() {
        return data.getTotalTimePercentage();
    }

    public Object getAvgTime() {
        return data.getAvgTime();
    }

    public Object getCallCount() {
        return data.getCallCount();
    }

    private void refreshArray() {
        childrenArray = new CCTNode[childrenList.size()];
        for (int i = 0; i < childrenList.size(); i++)
            childrenArray[i] = childrenList.get(i);
    }

    @Override
    public CCTNode getChild(int index) {
        return childrenList.get(index);
    }

    @Override
    public CCTNode[] getChildren() {
        return childrenArray;
    }

    @Override
    public int getIndexOfChild(Object child) {

        for (int i = 0; i < childrenList.size(); i++)
            if (child.equals(childrenList.get(i)))
                return i;

        return -1;
    }

    @Override
    public int getNChildren() {
        return childrenList.size();
    }

    @Override
    public CCTNode getParent() {
        return null;
    }

    @Override
    public String toString() {
        return getProcessName().toString();
    }

}
