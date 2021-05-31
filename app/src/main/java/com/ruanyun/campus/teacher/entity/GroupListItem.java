package com.ruanyun.campus.teacher.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 *  #(c) ruanyun PocketCampus <br/>
 *
 *  版本说明: $id:$ <br/>
 *
 *  功能说明: 校内通知
 * 
 *  <br/>创建说明: 2014-4-16 下午2:05:53 shengguo  创建文件<br/>
 * 
 *  修改历史:<br/>
 *
 */
public class GroupListItem {

	private String title;

	private JSONArray bannerArr;
	private JSONArray groupMenu;
	private JSONArray notifyArr;
	private JSONArray listArr;
	private int page;
	private int pagecount;
	private int menuId;
	private JSONArray MutiSelArr;

	public JSONArray getMutiSelArr() {
		return MutiSelArr;
	}

	public void setMutiSelArr(JSONArray mutiSelArr) {
		MutiSelArr = mutiSelArr;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPagecount() {
		return pagecount;
	}

	public void setPagecount(int pagecount) {
		this.pagecount = pagecount;
	}

	public int getMenuId() {
		return menuId;
	}

	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}

	public GroupListItem(JSONObject jo, GroupListItem oldlist) throws JSONException {
		title = jo.optString("标题显示");
		if(title==null && oldlist!=null)
			title=oldlist.getTitle();
		bannerArr = jo.optJSONArray("广告轮播");
		if(bannerArr==null && oldlist!=null)
			bannerArr=oldlist.getBannerArr();
		groupMenu= jo.optJSONArray("分组菜单");
		if(groupMenu==null && oldlist!=null)
			groupMenu=oldlist.getGroupMenu();
		notifyArr= jo.optJSONArray("公告列表");
		if(notifyArr==null && oldlist!=null)
			notifyArr=oldlist.getNotifyArr();
		listArr= jo.optJSONArray("列表数据");
		if(listArr==null && oldlist!=null)
			listArr=oldlist.getListArr();
		if(oldlist!=null && oldlist.getListArr()!=null)
		{
			for(int i=0;i<oldlist.getListArr().length();i++)
			{
				JSONObject item=oldlist.getListArr().optJSONObject(i);
				for (int j=0;j<listArr.length();j++)
				{
					JSONObject subitem=listArr.optJSONObject(j);
					if(subitem.optString("id").equals(item.optString("id"))) {
						subitem.put("checked", item.optBoolean("checked"));
						break;
					}
				}
			}
		}
		if(jo.optInt("page")>0)
			page=jo.optInt("page");
		else
			page=1;
		if(jo.optInt("pagecount")>0)
			pagecount=jo.optInt("pagecount");
		else
			pagecount=1;
		menuId=jo.optInt("menuId");
		MutiSelArr=jo.optJSONArray("多选按钮");
	}

	public JSONArray getNotifyArr() {
		return notifyArr;
	}

	public void setNotifyArr(JSONArray notifyArr) {
		this.notifyArr = notifyArr;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public JSONArray getBannerArr() {
		return bannerArr;
	}

	public void setBannerArr(JSONArray bannerArr) {
		this.bannerArr = bannerArr;
	}

	public JSONArray getGroupMenu() {
		return groupMenu;
	}

	public void setGroupMenu(JSONArray groupMenu) {
		this.groupMenu = groupMenu;
	}

	public JSONArray getListArr() {
		return listArr;
	}


}
