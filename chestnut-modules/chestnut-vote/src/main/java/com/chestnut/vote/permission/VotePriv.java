package com.chestnut.vote.permission;

public interface VotePriv {

	String Prefix = "vote:";

	public String View = Prefix + "view";

	public String Add = Prefix + "add";

	public String Edit = Prefix + "edit";

	public String Delete = Prefix + "delete";
}
