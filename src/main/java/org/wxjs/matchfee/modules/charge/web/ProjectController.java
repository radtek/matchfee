/**
 * Copyright &copy; 2012-2016 千里目 All rights reserved.
 */
package org.wxjs.matchfee.modules.charge.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wxjs.matchfee.common.config.Global;
import org.wxjs.matchfee.common.persistence.Page;
import org.wxjs.matchfee.common.web.BaseController;
import org.wxjs.matchfee.common.utils.StringUtils;
import org.wxjs.matchfee.modules.charge.entity.Project;
import org.wxjs.matchfee.modules.charge.service.ChargeService;
import org.wxjs.matchfee.modules.charge.service.ProjectService;

/**
 * 项目信息Controller
 * @author GLQ
 * @version 2017-11-27
 */
@Controller
@RequestMapping(value = "${adminPath}/charge/project")
public class ProjectController extends BaseController {

	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ChargeService chargeService;
	
	@ModelAttribute
	public Project get(@RequestParam(required=false) String id) {
		Project entity = null;
		if (StringUtils.isNotBlank(id)){
			entity = projectService.get(id);
		}
		if (entity == null){
			entity = new Project();
		}
		return entity;
	}
	
	@RequiresPermissions("charge:charge:view")
	@RequestMapping(value = {"list", ""})
	public String list(Project project, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<Project> page = projectService.findPage(new Page<Project>(request, response), project); 
		
		//debug
		
		for(Project prj : page.getList()){
			logger.debug(prj.toString());
		}
		
		model.addAttribute("page", page);
		return "modules/charge/projectList";
	}
	
	@RequiresPermissions("charge:charge:view")
	@RequestMapping(value = {"listLocalAndRemote"})
	public String listLocalAndRemote(Project project, HttpServletRequest request, HttpServletResponse response, Model model) {
		List<Project> list = projectService.findList_LocalAndRemote(project); 
		if(list.size()>100){
			model.addAttribute("list", list.subList(0, 99));
		}else{
			model.addAttribute("list", list);
		}
		return "modules/charge/projectList";
	}

	@RequiresPermissions("charge:charge:view")
	@RequestMapping(value = "form")
	public String form(Project project, Model model) {
		String prjNum = "";
		if(project!=null && !StringUtils.isBlank(project.getPrjNum())){
			prjNum = project.getPrjNum();
			project = projectService.getByPrjNum(prjNum);
			if(project == null){
				project = projectService.getByPrjNum_Remote(prjNum);
			}			
		}
		
		model.addAttribute("project", project);
		return "modules/charge/projectForm";
	}

	@RequiresPermissions("charge:charge:edit")
	@RequestMapping(value = "save")
	public String save(Project project, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, project)){
			return form(project, model);
		}
		projectService.save(project);
		addMessage(redirectAttributes, "保存项目信息成功");
		return "redirect:"+Global.getAdminPath()+"/charge/project/?repage";
	}
	
	@RequiresPermissions("charge:charge:edit")
	@RequestMapping(value = "saveAndContinue")
	public String saveAndContinue(Project project, HttpSession httpSession, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, project)){
			return form(project, model);
		}
		projectService.save(project);
		//addMessage(redirectAttributes, "保存项目信息成功");
		model.addAttribute("project", project);
		
		httpSession.setAttribute("project", project);
		
		return "redirect:"+Global.getAdminPath()+"/charge/charge/create?repage";
	}
	
	@RequiresPermissions("charge:charge:edit")
	@RequestMapping(value = "delete")
	public String delete(Project project, RedirectAttributes redirectAttributes) {
		projectService.delete(project);
		addMessage(redirectAttributes, "删除项目信息成功");
		return "redirect:"+Global.getAdminPath()+"/charge/project/?repage";
	}

}