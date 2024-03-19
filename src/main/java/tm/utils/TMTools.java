package tm.utils;

public class TMTools {
    public enum ToolType {
        SELECT_TOOL(1),
        ZOOM_TOOL(2),
        PICKUP_TOOL(3),
        BRUSH_TOOL(4),
        LINE_TOOL(5),
        FILL_TOOL(6),
        REPLACE_TOOL(7),
        MOVE_TOOL(8);

        private int fallbackInt;

        ToolType(final int fallbackInt) {
            this.fallbackInt = fallbackInt;
        }

        public int fallbackInt() {
            return fallbackInt;
        }
    }

//    public enum ToolEnum {
//        private ToolType TYPE = ToolType.SELECT_TOOL;
//    }
}
