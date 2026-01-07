export default [
  {
    url: '/mock/login',
    method: 'post',
    response: () => {
      return {
        code: 200,
        data: {
          "msg": "操作成功",
          "code": 200,
          "token": "eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2tleSI6IjI5M2NmNWRkLTNiNzItNDI0NC1iNGU2LTg3Mzg3ZGNiZDA3YSJ9.0Ab8h8ptK-Kp7oUSZStgInIszprWXBYUA0cxHsIR1LnEBjeIYIVLPDV09pPArc68bHWC6FRPEsrcdm6O8cJrJA"
        }
      }
    }
  },
  {
    url: '/mock/logout',
    method: 'post',
    response: () => {
      return {
        code: 200
      }
    }
  },
  {
    url: '/mock/getInfo',
    method: 'get',
    response: () => {
      return {
        "msg": "操作成功",
        "code": 200,
        "permissions": [
            "*:*:*"
        ],
        "roles": [
            "admin"
        ],
        "isDefaultModifyPwd": false,
        "isPasswordExpired": false,
        "user": {
            "createBy": "admin",
            "createTime": "2025-05-26 10:07:46",
            "updateBy": null,
            "updateTime": null,
            "remark": "管理员",
            "userId": 1,
            "deptId": 103,
            "userName": "admin",
            "nickName": "沙雕",
            "email": "ry@163.com",
            "phonenumber": "15888888888",
            "sex": "1",
            "avatar": "",
            "password": "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2",
            "status": "0",
            "delFlag": "0",
            "loginIp": "123.122.15.173",
            "loginDate": "2025-08-30T15:57:40.000+08:00",
            "pwdUpdateDate": "2025-05-26T10:07:46.000+08:00",
            "dept": {
                "createBy": null,
                "createTime": null,
                "updateBy": null,
                "updateTime": null,
                "remark": null,
                "deptId": 103,
                "parentId": 101,
                "ancestors": "0,100,101",
                "deptName": "研发部门",
                "orderNum": 1,
                "leader": "沙雕",
                "phone": null,
                "email": null,
                "status": "0",
                "delFlag": null,
                "parentName": null,
                "children": []
            },
            "roles": [
                {
                    "createBy": null,
                    "createTime": null,
                    "updateBy": null,
                    "updateTime": null,
                    "remark": null,
                    "roleId": 1,
                    "roleName": "超级管理员",
                    "roleKey": "admin",
                    "roleSort": 1,
                    "dataScope": "1",
                    "menuCheckStrictly": false,
                    "deptCheckStrictly": false,
                    "status": "0",
                    "delFlag": null,
                    "flag": false,
                    "menuIds": null,
                    "deptIds": null,
                    "permissions": null,
                    "admin": true
                }
            ],
            "roleIds": null,
            "postIds": null,
            "roleId": null,
            "admin": true
        }
      }
    }
  }
]