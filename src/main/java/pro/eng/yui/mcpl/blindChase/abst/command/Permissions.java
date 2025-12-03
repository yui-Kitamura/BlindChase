package pro.eng.yui.mcpl.blindChase.abst.command;

/**
 * Permission strings for BlindChase plugin commands and features.
 * All permissions start with 'blindchase.' prefix.
 */
public enum Permissions {
    JOIN("blindchase.join"),
    LEAVE("blindchase.leave"),
    REGENERATE("blindchase.regenerate"),
    ADMIN("blindchase.admin"),
    ALL("blindchase.all")
    ;

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    /** 実際のPermissionキーを取得 */
    public String get() {
        return permission;
    }
}
