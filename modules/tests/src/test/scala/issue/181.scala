// Copyright (c) 2018-2020 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package tests.issue

import cats.implicits._
import skunk._
import skunk.codec.all._
import skunk.implicits._
import tests.SkunkTest
import skunk.exception.PostgresErrorException

// https://github.com/tpolecat/skunk/issues/181
case object Test181 extends SkunkTest {

  def func(level: String): Command[Void] =
    sql"""
      CREATE OR REPLACE FUNCTION test181_#$level() RETURNS real AS $$$$
      BEGIN
          RAISE #$level 'This message contains non-ASCII characters: ü ø 😀 ש';
          RETURN 4.2;
      END;
      $$$$ LANGUAGE plpgsql;
    """.command

  sessionTest(s"non-ASCII error message (EXCEPTION)") { s =>
    for {
      _ <- s.execute(func("EXCEPTION"))
      _ <- s.unique(sql"select test181_EXCEPTION()".query(float4)).assertFailsWith[PostgresErrorException](show = true)
    } yield ("ok")
  }

  sessionTest(s"non-ASCII error message (WARNING)") { s =>
    for {
      _ <- s.execute(func("WARNING"))
      a <- s.unique(sql"select test181_WARNING()".query(float4))
      _ <- assertEqual("4.2", a, 4.2f)
    } yield ("ok")
  }

    sessionTest(s"non-ASCII error message (NOTICE)") { s =>
    for {
      _ <- s.execute(func("NOTICE"))
      a <- s.unique(sql"select test181_NOTICE()".query(float4))
      _ <- assertEqual("4.2", a, 4.2f)
    } yield ("ok")
  }

}