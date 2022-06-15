/// Menu
import Metismenu from "metismenujs";
import React, { Component, useContext, useEffect, useState } from "react";
/// Scroll
import PerfectScrollbar from "react-perfect-scrollbar";
/// Link
import { Link } from "react-router-dom";
import useScrollPosition from "use-scroll-position";
import { ThemeContext } from "../../../context/ThemeContext";
import {fetchAllMenu, fetchUserPermission} from "./../../../actions/menu";
import {connect} from "react-redux";
import _ from "lodash";
import LoadMenus from './../../components/Functions/LoadMenu'

// Image
//import profile from "../../../images/profile/pic1.jpg";

class MM extends Component {
  componentDidMount() {
    this.$el = this.el;
    this.mm = new Metismenu(this.$el);
  }
  componentWillUnmount() {
  }
  render() {
    return (
      <div className="mm-wrapper">
        <ul className="metismenu" ref={(el) => (this.el = el)}>
          {this.props.children}
        </ul>
      </div>
    );
  }
}

const SideBar = (props) => {
  const {
    iconHover,
    sidebarposition,
    headerposition,
    sidebarLayout,
  } = useContext(ThemeContext);

//const {menuItems,fetchExternalMenu2} = LoadMenus()

  useEffect(() => {
    var btn = document.querySelector(".nav-control");
    var aaa = document.querySelector("#main-wrapper");
    function toggleFunc() {
      return aaa.classList.toggle("menu-toggle");
    }
    btn.addEventListener("click", toggleFunc);
    fetchExternalMenu()
    fetchPermisisons()
  }, []);//props.menuList to continuous checking for menu list
  let scrollPosition = useScrollPosition();

  function userHasRole(role){
    const userRoles = props.permissions;
    if(role && role.length > 0 && _.intersection(role, userRoles).length === 0){
      return false;
    }
    return true;
  }
  const fetchExternalMenu = () => {
    const onSuccess = () => {
    }
    const onError = () => {
    }
    props.fetchAllExternalModulesMenu(onSuccess, onError);
  };

  const fetchPermisisons = () => {
    const onSuccess = () => {
    }
    const onError = () => {
    }
    props.fetchUserPermission(onSuccess, onError);
  };

  /// Path
  let path = window.location.pathname;
  path = path.split("/");
  path = path[path.length - 1];
  ///Admin Roles
  let roles =["admin_read"]
  //console.log(props.menuList)
  //method to determain main sidebar menu
  function sideBarParentUrl(menu){
          //menu.moduleId===null || menu.url!==null? menu.url : "modules", state: menu.url
          if(menu.moduleId===null && menu.url===null){
            return ""
          }else if(menu.moduleId===null && menu.url!==null){
            return menu.url
          }else if(menu.moduleId!==null || menu.url!==null){
            return ` "modules",  state: menu.url`
          }
  }
  /// Active menu
  let deshBoard = ["dashboard"],
    admin = [
      "users",
      "roles",
      "bootstrap-modules",
      "application-codeset",
      "organisation-unit",
      "application-matrics",
      "log-configuration",
      "health-checks",
      "system-configuration",
      "menu"
    ],
    sysInfo = ["application-matrics", "log-configuration", "health-checks", "system-configuration"];

    
    
  return (
    <div
      className={`deznav ${iconHover} ${
        sidebarposition.value === "fixed" &&
        sidebarLayout.value === "horizontal" &&
        headerposition.value === "static"
          ? scrollPosition > 120
            ? "fixed"
            : ""
          : ""
      }`}
      style={{top:'52px'}}
    >
      <PerfectScrollbar className="deznav-scroll" style={{paddingTop:'20px'}}>
        <MM className="metismenu" id="menu" style={{fontSize:'10px'}}>


          {props.menuList && props.menuList.map((menu, index) => (

              <>
                  <li className={`${admin.includes(path) ? "mm-active" : ""}`} style={{color: '#798087', padding: '2px'}}
                      show={"false"}>
                    <Link className={menu.subs && menu.subs.length>0 ?"has-arrow ai-icon":""}  to={{ pathname: menu.moduleId===null ? menu.url : "modules", state: menu.url}}
                          style={{color: '#798087', padding: '1px', backgroundColor: 'white'}}>
                      <i className={menu.icon!==null ? menu.icon : "flaticon-087-stop"} style={{color: '#24a4eb'}}/>
                      <span className="nav-text" style={{color: '#24a4eb'}}>{menu.name}</span>
                    </Link>
                    {menu.subs.length>0 ?
                        menu.subs.map((subMenu, index) => (
                            <>
                              <ul>
                                <li>
                                  <Link style={{color: '#798087'}} className={`${path === "users" ? "mm-active" : ""}`} to={subMenu.moduleId && subMenu.moduleId!==null? "modules": subMenu.url  }>
                                    {subMenu.name} {subMenu.moduleId}
                                  </Link>
                                  {subMenu.subs && subMenu.subs.length > 0 ?
                                      subMenu.subs.map((subSubMenu, index) => (
                                        <>
                                          <li className="ms-2">
                                            <Link style={{color: '#798087'}}
                                                  className={`${path === "log-configuration" ? "mm-active" : ""}`} to={!subSubMenu.moduleId || subSubMenu.moduleId===null? subSubMenu.url : "modules" }>
                                                      <span className="align-middle me-1">
                                                        <i className="ti-angle-right"></i>
                                                      </span>{" "}{subSubMenu.name}
                                            </Link>
                                          </li>
                                        </>
                                      ))
                                    :
                                      ""
                                  }
                                </li>
                              </ul>

                            </>
                        ))
                    :
                        ""

                    }

                  </li>
              </>
              ))}

        </MM>


      </PerfectScrollbar>
    </div>
  );
};

const mapStateToProps = (state, ownProps) => {
  return {
    menuList: state.menu.list,
    permissions: state.menu.permissions,
  };
};

const mapActionToProps = {
  fetchAllExternalModulesMenu: fetchAllMenu,
  fetchUserPermission: fetchUserPermission
};

export default connect(mapStateToProps, mapActionToProps)(SideBar);
