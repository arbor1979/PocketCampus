package com.ruanyun.campus.teacher.entity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 *  #(c) ruanyun PocketCampus <br/>
 *
 *  版本说明: $id:$ <br/>
 *
 *  功能说明: 成绩
 * 
 *  <br/>创建说明: 2014-4-16 下午6:41:34 shengguo  创建文件<br/>
 * 
 *  修改历史:<br/>
 *
 */
public class AchievementItem {
	private String templateName;
	private String title;
	private List<Achievement> achievements;
	private String rightButton;
	private String rightButtonURL;
	private String submitTarget;
	private String huizong;
	private int page;
	private int allnum;
	private JSONArray filterArr;
	private JSONArray MutiSelArr;
	private JSONArray groupArr;
	private int curGroupId=0;

	public JSONArray getMutiSelArr() {
		return MutiSelArr;
	}

	public void setMutiSelArr(JSONArray mutiSelArr) {
		MutiSelArr = mutiSelArr;
	}

	public JSONArray getGroupArr() {
		return groupArr;
	}

	public void setGroupArr(JSONArray groupArr) {
		this.groupArr = groupArr;
	}

	public int getCurGroup() {
		return curGroupId;
	}

	public void setCurGroupId(int curGroupId) {
		this.curGroupId = curGroupId;
	}

	public String getHuizong() {
		return huizong;
	}

	public void setHuizong(String huizong) {
		this.huizong = huizong;
	}

	public AchievementItem(JSONObject jo) {
		templateName = jo.optString("适用模板");
		title = jo.optString("标题显示");
		achievements = new ArrayList<Achievement>();
		JSONArray joa = jo.optJSONArray("成绩数值");
		if(joa!=null)
		{
			for (int i = 0; i < joa.length(); i++) {
				Achievement a = new Achievement(joa.optJSONObject(i));
				achievements.add(a);
			}
		}
		rightButton=jo.optString("右上按钮");
		rightButtonURL=jo.optString("右上按钮URL");
		submitTarget=jo.optString("右上按钮Submit");
		page=jo.optInt("page");
		allnum=jo.optInt("allnum");
		filterArr=jo.optJSONArray("过滤条件");
		MutiSelArr=jo.optJSONArray("多选按钮");
		groupArr=jo.optJSONArray("显示分组");
		if(filterArr==null)
			filterArr=new JSONArray();
		if(MutiSelArr==null)
			MutiSelArr=new JSONArray();
		if(groupArr==null)
			groupArr=new JSONArray();
		curGroupId=jo.optInt("当前分组");
		huizong=jo.optString("汇总");
	}
	public int getPage() {
		return page;
	}
	public int getAllnum() {
		return allnum;
	}
	public JSONArray getFilterArr() {
		return filterArr;
	}
	public String getSubmitTarget() {
		return submitTarget;
	}

	public void setSubmitTarget(String submitTarget) {
		this.submitTarget = submitTarget;
	}

	public String getRightButton() {
		return rightButton;
	}

	public void setRightButton(String rightButton) {
		this.rightButton = rightButton;
	}

	public String getRightButtonURL() {
		return rightButtonURL;
	}

	public void setRightButtonURL(String rightButtonURL) {
		this.rightButtonURL = rightButtonURL;
	}

	public class Achievement {
		private String id;// 编号
		private String icon;// 图标
		private String title;// 标题
		private String total;// 总分
		private String rank;// 排名
		private String detailUrl;// 详情地址
		private String thecolor;//总分颜色
		private String templateName;
	    private String templateGrade;
		private JSONObject extraMenu;
		private JSONObject iconLink;
		private String thirdline;
		private boolean ifChecked;
		private int progress;

		public String getThirdline() {
			return thirdline;
		}

		public void setThirdline(String thirdline) {
			this.thirdline = thirdline;
		}

		public boolean isIfChecked() {
			return ifChecked;
		}

		public void setIfChecked(boolean ifChecked) {
			this.ifChecked = ifChecked;
		}

		public int getProgress() {
			return progress;
		}

		public void setProgress(int progress) {
			this.progress = progress;
		}

		public String getTemplateName() {
			return templateName;
		}

		public void setTemplateName(String templateName) {
			this.templateName = templateName;
		}

		public String getTemplateGrade() {
			return templateGrade;
		}

		public void setTemplateGrade(String templateGrade) {
			this.templateGrade = templateGrade;
		}

		public JSONObject getIconLink() {
			return iconLink;
		}

		public Achievement(JSONObject jo) {
			id = jo.optString("编号");
			icon = jo.optString("图标");
			title = jo.optString("第一行");
			total = jo.optString("第二行左");
			rank = jo.optString("第二行右");
			detailUrl = jo.optString("内容项URL");
			thecolor=jo.optString("颜色");
			templateName = jo.optString("模板");
			templateGrade = jo.optString("模板级别");
			extraMenu= jo.optJSONObject("附加菜单");
			iconLink= jo.optJSONObject("图标链接");
			thirdline = jo.optString("第三行");
			if(jo.optString("进度条").length()>0)
				progress=jo.optInt("进度条");
			else
				progress=-1;
		}
		public JSONObject getExtraMenu() {
			return extraMenu;
		}
		public String getThecolor() {
			return thecolor;
		}

		public void setThecolor(String thecolor) {
			this.thecolor = thecolor;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getIcon() {
			return icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getTotal() {
			return total;
		}

		public void setTotal(String total) {
			this.total = total;
		}

		public String getRank() {
			return rank;
		}

		public void setRank(String rank) {
			this.rank = rank;
		}

		public String getDetailUrl() {
			return detailUrl;
		}

		public void setDetailUrl(String detailUrl) {
			this.detailUrl = detailUrl;
		}
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Achievement> getAchievements() {
		return achievements;
	}

	public void setAchievements(List<Achievement> achievements) {
		this.achievements = achievements;
	}
	
}
