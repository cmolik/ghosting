/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiger.effects.ghosting;

import java.util.HashMap;
import java.util.Iterator;
import scene.surface.mesh.Mesh;
import tiger.core.Link;

/**
 *
 * @author cmolikl
 */
public class Grouping implements Link<Grouping>{
    protected int nextGroupId = 0;
    protected int size = 0;
    protected HashMap<Integer, Integer> grouping = new HashMap<Integer, Integer>();
    protected HashMap<Integer, String> groupNames = new HashMap<Integer, String>();

    synchronized public int addToNewGroup(Mesh... meshes) {
        int groupId = nextGroupId;
        nextGroupId++;
        addToGroup(groupId, meshes);
        groupNames.put(groupId, meshes[0].getName());
        return groupId;
    }

    synchronized public int addToNewGroup(String groupName, Mesh... meshes) {
        int groupId = nextGroupId;
        nextGroupId++;
        addToGroup(groupId, meshes);
        groupNames.put(groupId, groupName);
        return groupId;
    }
    
    public void addToGroup(int groupId, Mesh... meshes) {
        if(!grouping.containsValue(groupId)) {
            size++;
        }
        for(Mesh mesh : meshes) {
            grouping.put(mesh.getId(), groupId);
        }
    }

    public void removeFromGroup(Mesh... meshes) {
        for(Mesh mesh : meshes) {
            int groupId = grouping.remove(mesh.getId());
            if(!grouping.containsValue(groupId)) {
                size--;
            }
        }
    }

    public int getGroup(Mesh mesh) {
        Integer groupId = grouping.get(mesh.getId());
        if(groupId == null) {
            return -1;
        }
        else {
            return groupId;
        }
    }

    public String setGroupName(int groupId, String groupName) {
        return groupNames.put(groupId, groupName);
    }

    public String getGroupName(int groupId) {
        return groupNames.get(groupId);
    }

    synchronized public int getSize() {
        return size;
    }

    public Grouping get() {
        return this;
    }

    public Iterator<Integer> getGroupIdIterator() {
        return grouping.values().iterator();
    }

}
