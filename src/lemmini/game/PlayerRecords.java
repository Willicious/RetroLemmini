package lemmini.game;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class to store player records per pack/group/level
 * @author Will James
 */
public class PlayerRecords {

    private final Map<String, PackRecord> packs = new LinkedHashMap<>();

    public Map<String, PackRecord> getPacks() {
        return packs;
    }

    public PackRecord getPack(String packName) {
        return packs.get(packName);
    }

    public PackRecord getOrCreatePack(String packName) {
        return packs.computeIfAbsent(packName, k -> new PackRecord());
    }

    public boolean hasPack(String packName) {
        return packs.containsKey(packName);
    }
    
    @Override
    public String toString() {
        return packs.toString();
    }
    
    /**
     * Class to store pack record
     * @author Will James
     */
    public class PackRecord {

        private final Map<String, GroupRecord> groups = new LinkedHashMap<>();

        public Map<String, GroupRecord> getGroups() {
            return groups;
        }

        public GroupRecord getGroup(String groupName) {
            return groups.get(groupName);
        }

        public GroupRecord getOrCreateGroup(String groupName) {
            return groups.computeIfAbsent(groupName, k -> new GroupRecord());
        }

        public boolean hasGroup(String groupName) {
            return groups.containsKey(groupName);
        }
        
        @Override
        public String toString() {
            return groups.toString();
        }
    }

    /**
     * Class to store group record
     * @author Will James
     */
    public class GroupRecord {

        private final Map<Integer, LevelRecord> levels = new LinkedHashMap<>();

        public Map<Integer, LevelRecord> getLevels() {
            return levels;
        }

        public LevelRecord getLevel(int level) {
            return levels.get(level);
        }

        public LevelRecord getLevelOrDefault(int level) {
            return levels.getOrDefault(level, LevelRecord.BLANK_LEVEL_RECORD);
        }

        public boolean hasLevel(int level) {
            return levels.containsKey(level);
        }
        
        public void setLevelRecord(int level, LevelRecord record) {
            if (record == null || !record.isCompleted())
                return;
            levels.put(level, record);
        }
        
        @Override
        public String toString() {
            return levels.toString();
        }
    }
}
