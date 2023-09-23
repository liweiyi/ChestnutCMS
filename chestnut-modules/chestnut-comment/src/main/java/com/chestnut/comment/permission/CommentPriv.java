package com.chestnut.comment.permission;

public interface CommentPriv {

	String Prefix = "comment:";

	public String View = Prefix + "view";

	public String Audit = Prefix + "audit";

	public String Delete = Prefix + "delete";
}
