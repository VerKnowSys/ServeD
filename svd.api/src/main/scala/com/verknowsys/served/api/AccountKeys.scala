/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api.accountkeys

import com.verknowsys.served.api._


/**
 * This module is related to operations on account keys
 *
 * @author teamon
 */
sealed abstract class Base extends ApiMessage

case object ListKeys extends Base

case class AddKey(key: AccessKey) extends Base

case class RemoveKey(key: AccessKey) extends Base
