package com.ruanyun.campus.teacher.entity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 *  #(c) ruanyun PocketCampus <br/>
 *
 *  版本说明: $id:$ <br/>
 *
 *  功能说明: 学生课堂总结
 * 
 *  <br/>创建说明: 2014-4-30 下午4:18:34 shengguo  创建文件<br/>
 * 
 *  修改历史:<br/>
 *
 */
public class StudentSummary {
	// {
	// "老师评价":"3",
	// "课程评价":"3",
	// "我的建议":"",
	// "课堂笔记":"",
	// "唯一码SEND":"用户_学生_1229641397____0________课程及老师评价_我的建议_课堂笔记________64013",
	// "课堂笔记图片":{
	// }
	// }
	private String id;
	private String teacherEvaluate;
	private String curriculumEvaluate;
	private String mySuggestion;
	private String classNotes;
	private String teacherkaoqin;
	private List<ImageItem> classNoteImages;
	private List<ImageItem> classAssignImages;
	private List<ImageItem> classSiduationImages;
	private String classroomDiscipline;
	private String classroomHealth;
	private String courseContent; // 授课内容
	private String homework; // 作业布置
	private String classroomSituation; // 课堂情况
	private String shouduanStr;
	private String pingtaiStr;
	private String ziyuankuStr;
	public String getTeacherkaoqin() {
		return teacherkaoqin;
	}

	public void setTeacherkaoqin(String teacherkaoqin) {
		this.teacherkaoqin = teacherkaoqin;
	}

	public String getClassroomDiscipline() {
		return classroomDiscipline;
	}

	public String getClassroomHealth() {
		return classroomHealth;
	}

	public String getCourseContent() {
		return courseContent;
	}

	public String getHomework() {
		return homework;
	}

	public String getClassroomSituation() {
		return classroomSituation;
	}

	public StudentSummary(JSONObject jo) {
		id = jo.optString("唯一码SEND");
		teacherEvaluate = jo.optString("老师评价");
		curriculumEvaluate = jo.optString("课程评价");
		mySuggestion = jo.optString("我的建议");
		classNotes = jo.optString("课堂笔记");
		teacherkaoqin=jo.optString("教师考勤情况");
		classNoteImages=new ArrayList<ImageItem>();
		JSONArray joii = jo.optJSONArray("课堂笔记图片");
		for (int i = 0; i < joii.length(); i++) {
			ImageItem imageInfo=new ImageItem(joii.optJSONObject(i));
			classNoteImages.add(imageInfo);
		}
		classAssignImages=new ArrayList<ImageItem>();
		joii = jo.optJSONArray("课堂作业图片");
		for (int i = 0; i < joii.length(); i++) {
			ImageItem imageInfo=new ImageItem(joii.optJSONObject(i));
			classAssignImages.add(imageInfo);
		}
		classSiduationImages=new ArrayList<ImageItem>();
		joii = jo.optJSONArray("课堂情况图片");
		for (int i = 0; i < joii.length(); i++) {
			ImageItem imageInfo=new ImageItem(joii.optJSONObject(i));
			classSiduationImages.add(imageInfo);
		}
		courseContent = jo.optString("授课内容");
		homework = jo.optString("作业布置");
		classroomSituation = jo.optString("课堂情况简要");
		classroomDiscipline = jo.optString("课堂纪律");
		classroomHealth = jo.optString("教室卫生");
		shouduanStr=jo.optString("信息化手段");
		pingtaiStr=jo.optString("教学平台");
		ziyuankuStr=jo.optString("资源库课程库名称");
	}

	public String getShouduanStr() {
		return shouduanStr;
	}

	public String getPingtaiStr() {
		return pingtaiStr;
	}

	public String getZiyuankuStr() {
		return ziyuankuStr;
	}

	public List<ImageItem> getClassSiduationImages() {
		return classSiduationImages;
	}

	public void setClassSiduationImages(List<ImageItem> classSiduationImages) {
		this.classSiduationImages = classSiduationImages;
	}

	public List<ImageItem> getClassAssignImages() {
		return classAssignImages;
	}

	public void setClassAssignImages(List<ImageItem> classAssignImages) {
		this.classAssignImages = classAssignImages;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTeacherEvaluate() {
		return teacherEvaluate;
	}

	public void setTeacherEvaluate(String teacherEvaluate) {
		this.teacherEvaluate = teacherEvaluate;
	}

	public String getCurriculumEvaluate() {
		return curriculumEvaluate;
	}

	public void setCurriculumEvaluate(String curriculumEvaluate) {
		this.curriculumEvaluate = curriculumEvaluate;
	}

	public String getMySuggestion() {
		return mySuggestion;
	}

	public void setMySuggestion(String mySuggestion) {
		this.mySuggestion = mySuggestion;
	}

	public String getClassNotes() {
		return classNotes;
	}

	public void setClassNotes(String classNotes) {
		this.classNotes = classNotes;
	}

	public List<ImageItem> getClassNoteImages() {
		return classNoteImages;
	}

	public void setClassNoteImages(List<ImageItem> classNoteImages) {
		this.classNoteImages = classNoteImages;
	}
}
